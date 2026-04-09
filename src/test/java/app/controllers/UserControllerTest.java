package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.persistence.entities.IEntity;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest
{
    private static final String ENDPOINT_URL = "/users";
    private static final int TEST_PORT = 7779;
    private static EntityManagerFactory emf;
    private static Javalin app;

    private Map<String, IEntity> seeded;
    private String headChefToken;
    private String sousChefToken;
    private String lineChefToken;
    private long gordonId;
    private long claireId;
    private long coldStationId;

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
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();

        seeded = populator.getSeededData();
        headChefToken = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");
        lineChefToken = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
        sousChefToken = TestAuthenticationUtil.bearerToken("marco@grill.com", "Hash3");
        gordonId = seeded.get("user_gordon").getId();
        claireId = seeded.get("user_claire").getId();
        coldStationId = seeded.get("station_cold").getId();
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Nested
    @DisplayName("GET /users/{id}")
    class GetById
    {
        @Test
        @DisplayName("Head Chef can get any user by ID")
        void getById()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + claireId)
                .then()
                .statusCode(200)
                .body("id", is((int) claireId))
                .body("email", is("claire@pastry.com"));
        }

        @Test
        @DisplayName("Get non-existing user returns 404")
        void getByIdNotFound() {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/999999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Line Cook is forbidden from getting specific user IDs")
        void getByIdForbidden()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + gordonId)
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("GET /users")
    class GetAll
    {
        @Test
        @DisplayName("Head Chef can see all users")
        void getAll()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(5));
        }

        @Test
        @DisplayName("Line Cook is forbidden from seeing all users")
        void getAllAsForbidden()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("PUT /users/{id}")
    class Update
    {
        @Test
        @DisplayName("User can update their own name")
        void update()
        {

            String payload = """
                {
                    "firstName": "Claire-Updated",
                    "lastName": "Smyth",
                    "stationId": %d
                }
                """.formatted(coldStationId);

            given()
                .header("Authorization", lineChefToken)
                .contentType("application/json")
                .body(payload)
                .when()
                .put(ENDPOINT_URL + "/" + claireId)
                .then()
                .statusCode(200)
                .body("firstName", is("Claire-Updated"));
        }

        @Test
        @DisplayName("Line Cook cannot update Gordon's profile")
        void updateOtherPersonForbidden()
        {
            String payload =
                """
                {
                "firstName": "Gordon Bleu",
                "lastName": "Ramsay",
                "stationId": 1
                }
                """;

            given()
                .header("Authorization", lineChefToken)
                .contentType("application/json")
                .body(payload)
                .when()
                .put(ENDPOINT_URL + "/" + gordonId)
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id}")
    class Delete
    {
        @Test
        @DisplayName("Head Chef can delete a Line Cook")
        void deleteUser()
        {
            Long reneId = seeded.get("user_rene").getId();

            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + reneId)
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("Head Chef cannot delete themselves")
        void deleteSelfConflict()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + gordonId)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Line Cooks cannot delete Users")
        void deleteLineCookForbidden()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + sousChefToken)
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("GET /users/me")
    class GetMe {

        @Test
        @DisplayName("Should return own profile")
        void getMe()
        {
            User claire = (User) seeded.get("user_claire");

            given()
                .header("Authorization", lineChefToken)
                .when()
                .get(ENDPOINT_URL + "/me")
                .then()
                .statusCode(200)
                .body("id", is(claire.getId().intValue()))
                .body("firstName", is(claire.getFirstName()))
                .body("lastName", is(claire.getLastName()))
                .body("email", is(claire.getEmail()))
                .body("userRole", is(claire.getUserRole().name()));
        }

        @Test
        @DisplayName("Missing Authentication returns 401")
        void getMeUnauthenticated()
        {
            given()
                .when()
                .get(ENDPOINT_URL + "/me")
                .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("PATCH /users/{id}/role")
    class RoleChange
    {
        @Test
        @DisplayName("Head Chef can promote a Line Cook")
        void changeRole()
        {
            String payload =
                """
                {
                "userRole": "SOUS_CHEF"
                }
                """;

            given()
                .header("Authorization", headChefToken)
                .contentType("application/json")
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + claireId + "/role")
                .then()
                .statusCode(200)
                .body("firstName", is("Claire"))
                .body("userRole", is("SOUS_CHEF"));
        }

        @Test
        @DisplayName("Sous chef cannot change role")
        void sousChefCantChangeRole()
        {
            String payload =
                """
                {
                "userRole": "HEAD_CHEF"
                }
                """;

            given()
                .header("Authorization", sousChefToken)
                .contentType("application/json")
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + claireId + "/role")
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("PATCH /users/{id}/email")
    class ChangeEmail
    {
        @Test
        @DisplayName("User can change their own email")
        void changeEmail()
        {
            String payload =
                """
                {
                "email": "claire@miseos.com"
                }
                """;

            given()
                .header("Authorization", lineChefToken)
                .contentType("application/json")
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + claireId + "/email")
                .then()
                .statusCode(200)
                .body("email", is("claire@miseos.com"));
        }

        @Test
        @DisplayName("Line Cook cannot change other users email")
        void changeOtherEmailForbidden()
        {
            String payload =
                """
                {
                "email": "claire@miseos.com"
                }
                """;

            given()
                .header("Authorization", lineChefToken)
                .contentType("application/json")
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + gordonId + "/email")
                .then()
                .statusCode(403);
        }
    }
    @Nested
    @DisplayName("PATCH /users/{id}/password")
    class PasswordChange
    {
        @Test
        @DisplayName("Line cook can change password and log in again")
        void changePassword()
        {
            String payload = """
                {
                    "currentPassword": "Hash2",
                    "newPassword": "ValidNewPass123!"
                }
                """;

            given()
                .header("Authorization", lineChefToken)
                .contentType("application/json")
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + claireId + "/password")
                .then()
                .statusCode(200);

            lineChefToken = TestAuthenticationUtil.bearerToken("claire@pastry.com", "ValidNewPass123!");

            given()
                .header("Authorization", lineChefToken)
                .when()
                .get(ENDPOINT_URL + "/me")
                .then()
                .statusCode(200)
                .body("firstName", is("Claire"));
        }

        @Test
        @DisplayName("Changing password with wrong current password returns 400")
        void wrongCurrentPassword()
        {
            String payload = """
                {
                    "currentPassword": "WrongPassword123",
                    "newPassword": "ValidNewPass1"
                }
                """;

            given()
                .header("Authorization", lineChefToken)
                .contentType("application/json")
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + claireId + "/password")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("PATCH /users/{id}/station/{stationId}")
    class AssignToStation
    {
        @Test
        @DisplayName("Sous Chef can assign a Line Cook to a new station")
        void assignStationSuccess()
        {
            Station grill = (Station) seeded.get("station_grill");

            given()
                .header("Authorization", sousChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + claireId + "/station/" + grill.getId())
                .then()
                .statusCode(200)
                .body("station.id", is(grill.getId().intValue()))
                .body("station.name", is(grill.getStationName()));
        }

        @Test
        @DisplayName("Line Cook cannot assign themselves to a station")
        void assignStationForbiddenForLineCook()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + claireId + "/station/" + coldStationId)
                .then()
                .statusCode(403);
        }
    }
}
