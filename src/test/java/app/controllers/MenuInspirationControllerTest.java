package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MenuInspirationControllerTest
{
    private static final String ENDPOINT_URL  = "/menu-inspirations";
    private static final int TEST_PORT = 7774;
    private static EntityManagerFactory emf;
    private static Javalin app;
    private String lineChefToken;

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
        lineChefToken = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Test
    @DisplayName("GET /menu-inspirations/daily - Should give 10 dish suggestion from ai integration")
    void getDailyInspiration()
    {
        given()
            .header("Authorization", lineChefToken)
            .when()
            .get(ENDPOINT_URL + "/daily")
            .then()
            .statusCode(200)
            .body(".", hasSize(10))
            .body("nameDA", everyItem(notNullValue()))
            .body("descriptionDA", everyItem(notNullValue()));
    }

    @Test
    @DisplayName("GET /menu-inspirations/daily - Should fail with 401 Unauthorized, with expired token")
    void getDailyInspirationShouldFailWithWrongToken()
    {
        given()
            .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqZWFuZXR0ZUBlbWFpbC5jb20iLCJyb2xlIjoiTElORV9DT09LIiwiaXNzIjoibWlzZU9TIiwiZXhwIjoxNzc0MDg4MjExLCJ1c2VySWQiOjUsImlhdCI6MTc3NDA4NzMxMSwiZW1haWwiOiJqZWFuZXR0ZUBlbWFpbC5jb20ifQ.8RyVKeyplMEMBZZ5rrOa2_-T2TZGaofR9d0GMHf36sU")
            .when()
            .get(ENDPOINT_URL + "/daily")
            .then()
            .statusCode(401)
            .body("message", equalToIgnoringCase("Token has expired"));
    }

    @Test
    @DisplayName("GET /menu-inspirations/daily - Should fail with 401 Unauthorized, with missing header")
    void getDailyInspirationShouldFailWitMissingHeader()
    {
        given()
            .when()
            .get(ENDPOINT_URL + "/daily")
            .then()
            .statusCode(401)
            .body("message", equalToIgnoringCase("Missing or malformed Authorization header"));
    }
}
