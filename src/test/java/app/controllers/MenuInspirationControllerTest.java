package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.persistence.entities.IEntity;
import app.persistence.entities.User;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MenuInspirationControllerTest
{
    private static final int TEST_PORT = 7774;
    private static final String USER_HEADER = "X-Dev-User-Id";

    private static EntityManagerFactory emf;
    private static Javalin app;
    private static Map<String, IEntity> seeded;

    @BeforeAll
    static void startServer()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(TEST_PORT, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = TEST_PORT;
        RestAssured.basePath = "/api/v1/menu-inspirations";
    }

    @BeforeEach
    void resetDatabase()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Test
    @DisplayName("GET /menu-inspirations/daily - Should give 3 dish suggestion from ai client")
    void getDailyInspiration()
    {
        User claire = (User) seeded.get("user_claire");

        given()
            .header(USER_HEADER, claire.getId())
            .when()
            .get("/daily")
            .then()
            .statusCode(200)
            .body(".", hasSize(10))
            .body("nameDA", everyItem(notNullValue()))
            .body("descriptionDA", everyItem(notNullValue()));
    }

    @Test
    @DisplayName("GET /menu-inspirations/daily - Should fail with 404 user not found with id not existing in db")
    void getDailyInspirationShouldFailWithNonStaffId()
    {
        given()
            .header(USER_HEADER, 999)
            .when()
            .get("/daily")
            .then()
            .statusCode(404)
            .body("message", equalToIgnoringCase("User with ID 999 was not found."));
    }
}
