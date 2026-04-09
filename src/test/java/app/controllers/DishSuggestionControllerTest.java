package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
import app.dtos.dishsuggestion.RejectDishSuggestionDTO;
import app.persistence.entities.*;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DishSuggestionControllerTest
{
    private static final String ENDPOINT_URL  = "/dish-suggestions";
    private static final int TEST_PORT = 7782;
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
        lineChefToken = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
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
                .header("Authorization", headChefToken)
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
    @DisplayName("GET /dish-suggestions")
    class GetAll
    {
        @Test
        @DisplayName("Should return all suggestions")
        void getAll()
        {
            List<Integer> ids = seeded.values().stream()
                .filter(DishSuggestion.class::isInstance)
                .map(DishSuggestion.class::cast)
                .map(ds -> ds.getId().intValue())
                .toList();

            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(5))
                .body("id", containsInAnyOrder(ids.toArray()));
        }

        @Test
        @DisplayName("Should return only PENDING suggestions")
        void filterByStatusPending()
        {
            given()
                .header("Authorization", headChefToken)
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
                .header("Authorization", headChefToken)
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
        @DisplayName("Should filter by week, year and status")
        void filterByStatusAndWeek()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("status", "PENDING")
                .queryParam("week", 7)
                .queryParam("year", 2026)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("targetWeek", everyItem(equalTo(7)))
                .body("targetYear", everyItem(equalTo(2026)))
                .body("dishStatus", everyItem(equalTo("PENDING")));
        }

        @Test
        @DisplayName("Should filter by Station (Cold)")
        void filterByStation()
        {
            Station cold = (Station) seeded.get("station_cold");

            given()
                .header("Authorization", headChefToken)
                .queryParam("stationId", cold.getId())
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("station.id", everyItem(equalTo(cold.getId().intValue())));
        }

        @Test
        @DisplayName("Should return 400 when week provided without year")
        void weekWithoutYearReturnsBadRequest()
        {
            given()
                .header("Authorization", headChefToken)
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
                .header("Authorization", headChefToken)
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
                .header("Authorization", headChefToken)
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
                .header("Authorization", headChefToken)
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
                .header("Authorization", lineChefToken)
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
                .header("Authorization", lineChefToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("PUT /dish-suggestions/{id}")
    class Update
    {
        @Test
        @DisplayName("Kitchen staff should successfully update suggestion")
        void kitchenStaffCanUpdate()
        {
            DishSuggestion steak = (DishSuggestion) seeded.get("suggestion_steak");
            Allergen milk = (Allergen) seeded.get("allergen_milk");
            User cookMarco = (User) seeded.get("user_marco");
            String marcoToken = TestAuthenticationUtil.bearerToken("marco@grill.com", "Hash3");

            DishSuggestionUpdateDTO dto = new DishSuggestionUpdateDTO(
                "Peberbøf",
                "Bøf af højreb serveret med med Madagascar peber sauce med cognac og fløde",
                Set.of(milk.getId())
            );

            given()
                .header("Authorization", marcoToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put(ENDPOINT_URL + "/" + steak.getId())
                .then()
                .statusCode(200)
                .body("nameDA", equalTo("Peberbøf"))
                .body("descriptionDA", equalTo("Bøf af højreb serveret med med Madagascar peber sauce med cognac og fløde"))
                .body("allergens", hasSize(1))
                .body("allergens.id", hasItem(milk.getId().intValue()))
                .body("createdBy.id", equalTo(cookMarco.getId().intValue()))
                .body("createdBy.firstName", equalTo(cookMarco.getFirstName()));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid characters in description")
        void invalidDescriptionReturnsBadRequest()
        {
            DishSuggestion steak = (DishSuggestion) seeded.get("suggestion_steak");

            DishSuggestionUpdateDTO dto = new DishSuggestionUpdateDTO(
                "Opdateret Peberbøf",
                "Nu med endnu mere pebersauce!",
                Set.of()
            );

            given()
                .header("Authorization", lineChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put(ENDPOINT_URL + "/" + steak.getId())
                .then()
                .statusCode(400)
                .body("message", equalTo("Description can only contain letters, numbers, and common symbols like '&', '-', or '.'."));
        }
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
                .header("Authorization", headChefToken)
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
        @DisplayName("Kitchen staff should get 401 when trying to approve suggestion")
        void kitchenStaffCannotApprove()
        {
            DishSuggestion salmon = (DishSuggestion) seeded.get("suggestion_salmon");

            given()
                .header("Authorization", lineChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + salmon.getId() + "/approve")
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Double approve should return 409")
        void doubleApproveReturnsBadRequest()
        {
            DishSuggestion salmon = (DishSuggestion) seeded.get("suggestion_salmon");

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + salmon.getId() + "/approve");

            given()
                .header("Authorization", headChefToken)
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
                .header("Authorization", headChefToken)
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
            RejectDishSuggestionDTO dto = new RejectDishSuggestionDTO("");

            given()
                .header("Authorization", headChefToken)
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

            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + roastbeef.getId())
                .then()
                .statusCode(204);


            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + roastbeef.getId())
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 when deleting unknown id")
        void deleteUnknownReturns404()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404);
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
        @DisplayName("Should return 401 when token expired")
        void invalidExpiredTokenReturns401()
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
