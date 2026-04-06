package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.persistence.entities.IEntity;
import app.persistence.entities.User;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityControllerTest
{
    private static final String ENDPOINT_URL = "/auth";
    private static final int TEST_PORT = 7780;
    private static EntityManagerFactory emf;
    private static Javalin app;
    private Map<String, IEntity> seeded;

    @BeforeAll
    static void startServer()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(TEST_PORT, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = TEST_PORT;
        RestAssured.basePath = "/api/v1";
    }

    @BeforeEach
    void setup()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator testPopulator = new TestPopulator(emf);
        testPopulator.populate();
        seeded = testPopulator.getSeededData();
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Nested
    @DisplayName("POST /register")
    class Create
    {
        @Test
        @DisplayName("Anyone can register a new user")
        void create()
        {
            String payload = """
                {
                    "firstName": "Jamie",
                    "lastName": "Oliver",
                    "email": "jamie@oliver.com",
                    "password": "Password123!"
                }
                """;

            given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/register")
                .then()
                .statusCode(201)
                .body("firstName", is("Jamie"))
                .body("lastName", is("Oliver"))
                .body("email", is("jamie@oliver.com"))
                .body("userRole", is("CUSTOMER"));
        }

        @Test
        @DisplayName("Create user with email already existing returns 409")
        void createMissingFieldReturn400()
        {
            String payload = """
                {
                    "firstName": "Jamie",
                    "lastName": "Oliver",
                    "email": "claire@pastry.com",
                    "password": "Password123!"
                }
                """;

            given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/register")
                .then()
                .statusCode(409)
                .body("message", equalToIgnoringCase("A user with this email already exists"));
        }

        @Test
        @DisplayName("Bad email format returns 400")
        void badEmailFormat()
        {
            String payload = """
                {
                    "firstName": "Jamie",
                    "lastName": "Oliver",
                    "email": "jamieoliver.com",
                    "password": "Password123!"
                }
                """;

            given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/register")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class Login
    {
        @Test
        @DisplayName("Valid credentials returns 200 with token and email")
        void validCredentialsReturnsToken()
        {
            String payload =
                """
                    {
                    "email": "gordon@kitchen.com",
                    "password": "Hash1"
                    }
                """;

            User gordon = (User) seeded.get("user_gordon");

            given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", is(gordon.getEmail()))
                .body("role", is(gordon.getUserRole().name()));
        }

        @Test
        @DisplayName("Wrong password returns 401")
        void wrongPasswordReturns401()
        {
            String payload =
                """
                    {
                    "email": "gordon@kitchen.com",
                    "password": "WrongPass"
                    }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/login")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Unknown email returns 401")
        void unknownEmailReturns401()
        {
            String payload =
                """
                    {
                    "email": "unknown@kitchen.com",
                    "password": "Hash1"
                    }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/login")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Missing password returns 400")
        void missingPasswordReturns400()
        {
            String payload =
                """
                    {
                    "email": "unknown@kitchen.com",
                    }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/login")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Empty body returns 400")
        void emptyBodyReturns400()
        {
            given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(ENDPOINT_URL + "/login")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("Token validation — authenticate filter")
    class TokenValidation
    {
        @Test
        @DisplayName("Missing token on protected endpoint returns 401")
        void missingTokenReturns401()
        {
            given()
                .when()
                .get("/users/me")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Malformed token returns 401")
        void malformedTokenReturns401()
        {
            given()
                .header("Authorization", "Bearer not.a.valid.token")
                .when()
                .get("/users/me")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Wrong scheme returns 401")
        void wrongSchemeReturns401()
        {
            given()
                .header("Authorization", "Basic gordon@kitchen.com:Hash1")
                .when()
                .get("/users/me")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Valid token gives access")
        void validTokenGivesAccess()
        {
            String token = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");

            given()
                .header("Authorization", token)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("Role authorization — authorize filter")
    class RoleAuthorization
    {
        @Test
        @DisplayName("Line cook cannot access head chef endpoint — returns 403")
        void lineCookBlockedFromHeadChefEndpoint()
        {
            String token = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");

            given()
                .header("Authorization", token)
                .when()
                .get("/users")
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Head chef can access kitchen staff endpoint")
        void headChefCanAccessKitchenStaffEndpoint()
        {
            String token = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");

            given()
                .header("Authorization", token)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200);
        }
    }
}
