package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.dishsuggestion.RejectDishSuggestionDTO;
import app.persistence.entities.Allergen;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.IEntity;
import app.persistence.entities.User;
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
class DishSuggestionControllerTest
{
    private static final String ENDPOINT_URL  = "/dish-suggestions";
    private static final int TEST_PORT = 7777;
    private static final String HEAD_CHEF_HEADER = "X-Dev-User-Id";
    private static EntityManagerFactory emf;
    private static Javalin app;
    private static Map<String, IEntity> seeded;

    @BeforeAll
    static void startServer()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(TEST_PORT, emf);

        RestAssured.baseURI  = "http://localhost";
        RestAssured.port     = TEST_PORT;
        RestAssured.basePath = "/api/v1";
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
        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }

    @Nested
    @DisplayName("GET /dish-suggestions/{id}")
    class GetById
    {

        @Test
        @DisplayName("Should return suggestion with correct fields")
        void returnsCorrectSuggestion()
        {
            DishSuggestion salmon = (DishSuggestion) seeded.get("suggestion_salmon");

            given()
                .when()
                .get(ENDPOINT_URL + "/" + salmon.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(salmon.getId().intValue()))
                .body("nameDA", equalTo(salmon.getNameDA()))
                .body("descriptionDA", equalTo(salmon.getDescriptionDA()))
                .body("createdBy.id", equalTo(salmon.getCreatedBy().getId().intValue()))
                .body("createdBy.firstName", equalTo(salmon.getCreatedBy().getFirstName()))
                .body("createdBy.lastName", equalTo(salmon.getCreatedBy().getLastName()))
                .body("station.id", is(salmon.getStation().getId().intValue()))
                .body("station.name", is(salmon.getStation().getStationName()))
                .body("allergens", hasSize(greaterThanOrEqualTo(0)))
                .body("allergens.id", everyItem(notNullValue()))
                .body("allergens.nameDA", everyItem(notNullValue()))
                .body("allergens.nameEN", everyItem(notNullValue()))
                .body("allergens.displayNumber", everyItem(greaterThan(0)));
        }

        @Test
        @DisplayName("Should return 404 for unknown id")
        void notFoundReturns404()
        {
            given()
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
                .when()
                .get(ENDPOINT_URL + "/-1")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /dish-suggestions")
    class GetAll
    {

    @Test
    @DisplayName("Should return all suggestions")
    void getAll()
    {
        given()
            .when()
            .get(ENDPOINT_URL)
            .then()
            .body("$", hasSize(5))
            .statusCode(200);
    }

        @Test
        @DisplayName("Should return only PENDING suggestions")
        void filterByStatusPending()
        {
            given()
                .queryParam("status", "PENDING")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("dishStatus", everyItem(equalTo("PENDING")));
        }

        @Test
        @DisplayName("Should filter by week and year")
        void filterByWeekAndYear()
        {
            DishSuggestion steak = (DishSuggestion) seeded.get("suggestion_steak");

            given()
                .queryParam("week", steak.getTargetWeek())
                .queryParam("year", steak.getTargetYear())
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("targetWeek", everyItem(equalTo(steak.getTargetWeek())))
                .body("targetYear", everyItem(equalTo(steak.getTargetYear())));
        }

        @Test
        @DisplayName("Should return 400 when week provided without year")
        void weekWithoutYearReturnsBadRequest()
        {
            given()
                .queryParam("week", 7)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 for invalid status value")
        void invalidStatusReturnsBadRequest()
        {
            given()
                .queryParam("status", "NONSENSE")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /dish-suggestions/current-week")
    class GetCurrentWeek
    {
        @Test
        @DisplayName("Should return a list (may be empty if no suggestions this week)")
        void returnsListForCurrentWeek()
        {
            given()
                .when()
                .get(ENDPOINT_URL + "/current-week")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void invalidStatusReturnsBadRequest()
        {
            given()
                .queryParam("status", "BADVALUE")
                .when()
                .get(ENDPOINT_URL + "/current-week")
                .then()
                .statusCode(400);
        }
    }


    @Nested
    @DisplayName("POST /dish-suggestions")
    class Create
    {
        @Test
        @DisplayName("Should create suggestion and return 201 with allergens")
        void createReturnsSuggestionWithAllergens()
        {
            User lineCook = (User) seeded.get("user_claire");
            Allergen gluten = (Allergen) seeded.get("allergen_gluten");
            Allergen milk = (Allergen) seeded.get("allergen_milk");

            String body = """
            {
              "nameDA": "Grillet torsk",
              "descriptionDA": "Saftig torsk med urter og citron",
              "stationId": 1,
              "allergenIds": [%d, %d],
              "targetWeek": 20,
              "targetYear": 2026
            }
            """.formatted(gluten.getId(), milk.getId());

            given()
                .header(HEAD_CHEF_HEADER, lineCook.getId())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(201)
                .body("nameDA", equalTo("Grillet torsk"))
                .body("dishStatus", equalTo("PENDING"))
                .body("allergens", hasSize(2))
                .body("allergens.id", hasItems(
                    gluten.getId().intValue(),
                    milk.getId().intValue()
                ));
        }

        @Test
        @DisplayName("Should return 400 for blank nameDA")
        void blankNameReturnsBadRequest()
        {
            User lineCook = (User) seeded.get("user_claire");

            String body = """
            {
              "nameDA": "",
              "descriptionDA": "Saftig torsk med urter og citron",
              "stationId": 1,
              "allergenIds": [],
              "targetWeek": 20,
              "targetYear": 2026
            }
            """;

            given()
                .header(HEAD_CHEF_HEADER, lineCook.getId())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }
    }

    @Test
    void update()
    {
    }

    @Nested
    @DisplayName("PATCH /dish-suggestions/{id}/approve")
    class Approve
    {
        @Test
        @DisplayName("HEAD CHEF should approve")
        void headChefApproves()
        {
            DishSuggestion salmon = (DishSuggestion) seeded.get("suggestion_salmon");
            User headChef = (User) seeded.get("user_gordon");

            given()
                .header(HEAD_CHEF_HEADER, headChef.getId())
                .when()
                .patch(ENDPOINT_URL + "/" + salmon.getId() + "/approve")
                .then()
                .statusCode(200)
                .body("dishStatus", equalTo("APPROVED"))
                .body("reviewedBy.id", equalTo(headChef.getId().intValue()))
                .body("reviewedAt", notNullValue())
                .body("allergens", hasSize(salmon.getAllergens().size()))
                .body("allergens.id", everyItem(notNullValue()));
        }

        @Test
        @DisplayName("LINE COOK should get 401 when trying to approve")
        void lineCookCannotApprove()
        {
            DishSuggestion salmon = (DishSuggestion) seeded.get("suggestion_salmon");
            User lineCook = (User) seeded.get("user_claire");

            given()
                .header(HEAD_CHEF_HEADER, lineCook.getId())
                .when()
                .patch(ENDPOINT_URL + "/" + salmon.getId() + "/approve")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Double approve should return 400")
        void doubleApproveReturnsBadRequest()
        {
            DishSuggestion salmon = (DishSuggestion) seeded.get("suggestion_salmon");
            User headChef = (User) seeded.get("user_gordon");

            given()
                .header(HEAD_CHEF_HEADER, headChef.getId())
                .when()
                .patch(ENDPOINT_URL + "/" + salmon.getId() + "/approve");

            given()
                .header(HEAD_CHEF_HEADER, headChef.getId())
                .when()
                .patch(ENDPOINT_URL + "/" + salmon.getId() + "/approve")
                .then()
                .statusCode(409);
        }
    }

    @Nested
    @DisplayName("PATCH /dish-suggestions/{id}/reject")
    class Reject
    {
        @Test
        @DisplayName("HEAD CHEF should reject with feedback")
        void headChefRejects()
        {
            DishSuggestion salmon = (DishSuggestion) seeded.get("suggestion_salmon");
            User headChef = (User) seeded.get("user_gordon");
            RejectDishSuggestionDTO dto = new RejectDishSuggestionDTO("For tung ret til sommermenu");

            given()
                .header(HEAD_CHEF_HEADER, headChef.getId())
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .patch(ENDPOINT_URL + "/" + salmon.getId() + "/reject")
                .then()
                .statusCode(200)
                .body("dishStatus", equalTo("REJECTED"))
                .body("feedback",equalTo(dto.feedback()))
                .body("reviewedBy.id",equalTo(headChef.getId().intValue()));
        }

        @Test
        @DisplayName("Should return 400 for blank feedback")
        void blankFeedbackReturnsBadRequest()
        {
            DishSuggestion salmon = (DishSuggestion) seeded.get("suggestion_salmon");
            User headChef = (User) seeded.get("user_gordon");
            RejectDishSuggestionDTO dto = new RejectDishSuggestionDTO("");

            given()
                .header(HEAD_CHEF_HEADER, headChef.getId())
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .patch(ENDPOINT_URL + "/" + salmon.getId() + "/reject")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /dish-suggestions/{id}")
    class Delete
    {
        @Test
        @DisplayName("HEAD CHEF should delete any suggestion")
        void headChefCanDelete()
        {
            DishSuggestion roastbeef = (DishSuggestion) seeded.get("suggestion_roastbeef");
            User headChef = (User) seeded.get("user_gordon");

            given()
                .header(HEAD_CHEF_HEADER, headChef.getId())
                .when()
                .delete(ENDPOINT_URL + "/" + roastbeef.getId())
                .then()
                .statusCode(204);


            given()
                .when()
                .get(ENDPOINT_URL + "/" + roastbeef.getId())
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 when deleting unknown id")
        void deleteUnknownReturns404()
        {
            User headChef = (User) seeded.get("user_gordon");

            given()
                .header(HEAD_CHEF_HEADER, headChef.getId())
                .when()
                .delete(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404);
        }
    }
}
