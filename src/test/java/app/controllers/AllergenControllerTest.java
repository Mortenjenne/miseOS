package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.allergen.AllergenCreateRequestDTO;
import app.dtos.allergen.AllergenUpdateRequestDTO;
import app.persistence.entities.Allergen;
import app.persistence.entities.IEntity;
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
class AllergenControllerTest
{
    private static final String ENDPOINT_URL = "/allergens";
    private static final int TEST_PORT = 7773;
    private static EntityManagerFactory emf;
    private static Javalin app;

    private Map<String, IEntity> seeded;
    private String headChefToken;
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

        seeded = populator.getSeededData();
        headChefToken = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");
        lineChefToken  = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Nested
    @DisplayName("GET /allergens")
    class GetAll
    {
        @Test
        @DisplayName("Should return all seeded allergens")
        void returnsAll()
        {
            given()
                .when()
                .header("Authorization", lineChefToken)
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(14))
                .body("id", everyItem(notNullValue()))
                .body("nameDA", everyItem(notNullValue()))
                .body("nameEN", everyItem(notNullValue()))
                .body("displayNumber", everyItem(greaterThan(0)));
        }
    }

    @Nested
    @DisplayName("GET /allergens/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return correct allergen")
        void returnsCorrectAllergen()
        {
            Allergen gluten = (Allergen) seeded.get("allergen_gluten");

            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + gluten.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(gluten.getId().intValue()))
                .body("nameDA", equalTo(gluten.getNameDA()))
                .body("nameEN", equalTo(gluten.getNameEN()))
                .body("descriptionDA", equalTo(gluten.getDescriptionDA()))
                .body("descriptionEN", equalTo(gluten.getDescriptionEN()))
                .body("displayNumber", equalTo(gluten.getDisplayNumber()));
        }

        @Test
        @DisplayName("Should return 404 for unknown id")
        void notFoundReturns404()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .get(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 400 for negative id")
        void negativeIdReturns400()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .get(ENDPOINT_URL + "/-1")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /allergens/name/{name}")
    class GetByName
    {
        @Test
        @DisplayName("Should return allergen matching Danish name")
        void returnsMatchingAllergen()
        {
            Allergen gluten = (Allergen) seeded.get("allergen_gluten");

            given()
                .header("Authorization", lineChefToken)
                .when()
                .get(ENDPOINT_URL + "/name/" + gluten.getNameDA())
                .then()
                .statusCode(200)
                .body("nameDA", equalTo(gluten.getNameDA()))
                .body("nameEN", equalTo(gluten.getNameEN()));
        }

        @Test
        @DisplayName("Should return 404 for unknown name")
        void unknownNameReturns404()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .get(ENDPOINT_URL + "/name/Hvidløg")
                .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("POST /allergens")
    class Create
    {
        @Test
        @DisplayName("Should create allergen and return 201")
        void createsAllergen()
        {
            AllergenCreateRequestDTO dto = new AllergenCreateRequestDTO(
                "Hvidløg",
                "Garlic",
                "Hvidløg og produkter heraf",
                "Garlic and products thereof",
                15
            );

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("nameDA", equalTo("Hvidløg"))
                .body("nameEN", equalTo("Garlic"))
                .body("displayNumber", equalTo(15));
        }

        @Test
        @DisplayName("Should return 400 when nameDA is blank")
        void blankNameDAReturnsBadRequest()
        {
            AllergenCreateRequestDTO dto = new AllergenCreateRequestDTO(
                "",
                "Garlic",
                "Hvidløg og produkter heraf",
                "Garlic and products thereof",
                15
            );

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 when displayNumber is not positive")
        void invalidDisplayNumberReturnsBadRequest()
        {
            AllergenCreateRequestDTO dto = new AllergenCreateRequestDTO(
                "Hvidløg",
                "Garlic",
                "Hvidløg og produkter heraf",
                "Garlic and products thereof",
                0
            );

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 409 when allergen with same name already exists")
        void duplicateNameReturnsConflict()
        {
            Allergen gluten = (Allergen) seeded.get("allergen_gluten");

            AllergenCreateRequestDTO dto = new AllergenCreateRequestDTO(
                gluten.getNameDA(),
                gluten.getNameEN(),
                gluten.getDescriptionDA(),
                gluten.getDescriptionEN(),
                gluten.getDisplayNumber()
            );

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(409);
        }
    }

    @Nested
    @DisplayName("PUT /allergens/{id}")
    class Update
    {
        @Test
        @DisplayName("Should update allergen and return updated fields")
        void updatesAllergen()
        {
            Allergen milk = (Allergen) seeded.get("allergen_milk");

            AllergenUpdateRequestDTO dto = new AllergenUpdateRequestDTO(
                "Laktose (Mælk)",
                milk.getNameEN(),
                milk.getDescriptionDA(),
                milk.getDescriptionEN(),
                milk.getDisplayNumber()
            );

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put(ENDPOINT_URL + "/" + milk.getId())
                .then()
                .statusCode(200)
                .body("nameDA", equalTo("Laktose (Mælk)"))
                .body("nameEN", equalTo(milk.getNameEN()));
        }

        @Test
        @DisplayName("Should return 404 when updating unknown id")
        void unknownIdReturns404()
        {
            AllergenUpdateRequestDTO dto = new AllergenUpdateRequestDTO(
                "Hvidløg",
                "Garlic",
                "Beskrivelse",
                "Description",
                15
            );

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 400 when nameEN is blank")
        void blankNameENReturnsBadRequest()
        {
            Allergen milk = (Allergen) seeded.get("allergen_milk");

            AllergenUpdateRequestDTO dto = new AllergenUpdateRequestDTO(
                milk.getNameDA(),
                "",
                milk.getDescriptionDA(),
                milk.getDescriptionEN(),
                milk.getDisplayNumber()
            );

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put(ENDPOINT_URL + "/" + milk.getId())
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /allergens/{id}")
    class Delete
    {
        @Test
        @DisplayName("Should delete allergen and return 204")
        void deletesAllergen()
        {
            Allergen nuts = (Allergen) seeded.get("allergen_nuts");

            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + nuts.getId())
                .then()
                .statusCode(204);

            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + nuts.getId())
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 when deleting unknown id")
        void unknownIdReturns404()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 400 for negative id")
        void negativeIdReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/-1")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("POST /allergens/seed")
    class Seed
    {
        @Test
        @DisplayName("Second seed call should return 400 — already seeded")
        void secondSeedIsBlocked()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .post(ENDPOINT_URL + "/seed")
                .then()
                .statusCode(400)
                .body("message", containsString("already seeded"));
        }
    }

    @Nested
    @DisplayName("Security & Authorization Tests")
    class Security
    {
        @Test
        @DisplayName("Should return 401 when no Authorization header is provided")
        void missingTokenReturns401()
        {
            given()
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(401)
                .body("message", equalToIgnoringCase("Missing or malformed Authorization header"));
        }

        @Test
        @DisplayName("Should return 401 when Authorization header is invalid")
        void invalidTokenReturns401()
        {
            given()
                .header("Authorization", "Bearer not-a-real-jwt-token")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(401)
                .body("message", equalToIgnoringCase("Token could not be verified"));
        }

        @Test
        @DisplayName("Should return 403 when token expired")
        void invalidExpiredTokenReturns403()
        {
            given()
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqZWFuZXR0ZUBlbWFpbC5jb20iLCJyb2xlIjoiTElORV9DT09LIiwiaXNzIjoibWlzZU9TIiwiZXhwIjoxNzc0MDg4MjExLCJ1c2VySWQiOjUsImlhdCI6MTc3NDA4NzMxMSwiZW1haWwiOiJqZWFuZXR0ZUBlbWFpbC5jb20ifQ.8RyVKeyplMEMBZZ5rrOa2_-T2TZGaofR9d0GMHf36sU")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(401)
                .body("message", equalToIgnoringCase("Token has expired"));
        }
    }
}
