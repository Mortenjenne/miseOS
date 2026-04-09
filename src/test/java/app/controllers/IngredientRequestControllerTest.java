package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.ingredient.IngredientRequestDTO;
import app.enums.RequestType;
import app.enums.Status;
import app.persistence.entities.*;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IngredientRequestControllerTest
{
    private static final String ENDPOINT_URL = "/ingredient-requests";
    private static final int TEST_PORT = 7778;
    private static EntityManagerFactory emf;
    private static Javalin app;

    private Map<String, IEntity> seeded;
    private String headChefToken;
    private String lineCookToken;
    private long reqDillId;
    private long reqTruffleId;

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
        reqDillId = seeded.get("req_dill").getId();
        reqTruffleId = seeded.get("req_truffle").getId();

        headChefToken = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");
        lineCookToken  = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Nested
    @DisplayName("PATCH /ingredient-requests/{id}/approve")
    class Approve
    {
        @Test
        @DisplayName("Approve request with payload")
        void approveWithPayload()
        {
            String payload = """
                {
                  "quantity": 12.5,
                  "note": "Approved with adjusted quantity"
                }
                """;
            IngredientRequest dill = (IngredientRequest) seeded.get("req_dill");

            IngredientRequestDTO response = given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + dill.getId() + "/approve")
                .then()
                .statusCode(200)
                .extract()
                .as(IngredientRequestDTO.class);

            assertThat(response.id(), is(dill.getId()));
            assertThat(response.status(), is(Status.APPROVED));
            assertThat(response.quantity(), is(12.5));
            assertThat(response.note(), is("Approved with adjusted quantity"));
            assertThat(response.reviewedAt(), is(notNullValue()));
        }

        @Test
        @DisplayName("Approve request - ok")
        void approve()
        {
            IngredientRequest dill = (IngredientRequest) seeded.get("req_dill");

            IngredientRequestDTO response = given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .when()
                .patch(ENDPOINT_URL + "/" + dill.getId() + "/approve")
                .then()
                .statusCode(200)
                .extract()
                .as(IngredientRequestDTO.class);

            assertThat(response.id(), is(dill.getId()));
            assertThat(response.status(), is(Status.APPROVED));
            assertThat(response.quantity(), is(dill.getQuantity()));
            assertThat(response.note(), is(dill.getNote()));
            assertThat(response.reviewedAt(), is(notNullValue()));
        }

        @Test
        @DisplayName("Line cook cannot approve — returns 403")
        void lineCookCannotApproveReturns403()
        {
            given()
                .header("Authorization", lineCookToken)
                .when()
                .patch(ENDPOINT_URL + "/" + reqDillId + "/approve")
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Already approved request - returns 409")
        void alreadyApprovedReturns409()
        {

            given().header("Authorization", headChefToken)
                .patch(ENDPOINT_URL + "/" + reqDillId + "/approve");

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + reqDillId + "/approve")
                .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("Unknown request - returns 404")
        void unknownRequestReturns404()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/9999/approve")
                .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PATCH /ingredient-requests/{id}/reject")
    class Reject
    {
        @Test
        @DisplayName("Reject request")
        void reject()
        {
            IngredientRequest truffle = (IngredientRequest) seeded.get("req_truffle");

            IngredientRequestDTO response = given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + truffle.getId() + "/reject")
                .then()
                .statusCode(200)
                .extract()
                .as(IngredientRequestDTO.class);

            assertThat(response.id(), is(truffle.getId()));
            assertThat(response.status(), is(Status.REJECTED));
            assertThat(response.reviewedAt(), is(notNullValue()));
        }
    }

    @Nested
    @DisplayName("GET /ingredient-requests/{id}")
    class GetById
    {
        @Test
        @DisplayName("Get by id should return correct ingredient request")
        void getById()
        {
            IngredientRequest dill = (IngredientRequest) seeded.get("req_dill");

            IngredientRequestDTO response = given()
                .header("Authorization", lineCookToken)
                .when()
                .get(ENDPOINT_URL + "/" + dill.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(IngredientRequestDTO.class);

            assertThat(response.id(), is(dill.getId()));
            assertThat(response.name(), is(dill.getName()));
            assertThat(response.status(), is(Status.PENDING));
            assertThat(response.requestedBy(), is(notNullValue()));
        }
    }

    @Nested
    @DisplayName("GET /ingredient-requests")
    class GetAll
    {
        @Test
        @DisplayName("Get all by date and pending")
        void getAllByDateAndPending()
        {
            String deliveryDate = LocalDate.now().plusDays(2).toString();

            List<IngredientRequestDTO> response = given()
                .header("Authorization", headChefToken)
                .queryParam("status", "PENDING")
                .queryParam("deliveryDate", deliveryDate)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", IngredientRequestDTO.class);

            assertThat(response, is(not(empty())));
            assertThat(response.stream().map(IngredientRequestDTO::status).toList(), everyItem(is(Status.PENDING)));
            assertThat(response.stream().map(r -> r.deliveryDate().toString()).toList(), everyItem(is(deliveryDate)));
        }

        @Test
        @DisplayName("Get all pending request by station")
        void getAllByStationAndPending()
        {
            Station cold = (Station) seeded.get("station_cold");
            IngredientRequest dill = (IngredientRequest) seeded.get("req_dill");

            List<IngredientRequestDTO> response = given()
                .header("Authorization", headChefToken)
                .queryParam("status", "PENDING")
                .queryParam("stationId", cold.getId())
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", IngredientRequestDTO.class);

            assertThat(response, is(not(empty())));
            assertThat(response, hasSize(1));

            IngredientRequestDTO result = response.get(0);
            assertThat(result.id(), is(dill.getId()));
            assertThat(result.name(), is(dill.getName()));
            assertThat(result.status(), is(Status.PENDING));
            assertThat(result.dish(), is(notNullValue()));
            assertThat(result.requestedBy(), is(notNullValue()));
        }

        @Test
        @DisplayName("Invalid status returns 400")
        void invalidStatusReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("status", "PEN")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Invalid deliveryDate format returns 400")
        void invalidDateFormatReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("deliveryDate", "not-a-date")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Head chef sees all requests")
        void headChefSeesAllRequests()
        {
            List<IngredientRequestDTO> response = given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", IngredientRequestDTO.class);

            Long distinctUsers = response.stream()
                .map(r -> r.requestedBy().id())
                .distinct()
                .count();

            assertThat(response, is(not(empty())));
            assertThat(distinctUsers, greaterThan(1L));
        }
    }

    @Nested
    @DisplayName("POST /ingredient-requests")
    class Create
    {
        @Test
        @DisplayName("create ok")
        void create()
        {
            Dish boeuf = (Dish) seeded.get("dish_boeuf");

            String payload = """
                {
                  "name": "Smør",
                  "quantity": 3.0,
                  "unit": "KG",
                  "preferredSupplier": "Inco",
                  "note": "Til sauce",
                  "requestType": "DISH_SPECIFIC",
                  "deliveryDate": "%s",
                  "dishId": %d
                }
                """.formatted(LocalDate.now().plusDays(3), boeuf.getId());

            IngredientRequestDTO response = given()
                .header("Authorization", lineCookToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(201)
                .extract()
                .as(IngredientRequestDTO.class);

            assertThat(response.id(), is(notNullValue()));
            assertThat(response.name(), is("Smør"));
            assertThat(response.status(), is(Status.PENDING));
            assertThat(response.dish(), is(notNullValue()));
            assertThat(response.dish().id(), is(boeuf.getId()));
        }

        @Test
        @DisplayName("Line cook cannot request dish from another station")
        void dishFromWrongStationReturns403()
        {
            Dish salmon = (Dish) seeded.get("dish_salmon");

            String payload = """
            {
              "name": "Smør",
              "quantity": 3.0,
              "unit": "KG",
              "requestType": "DISH_SPECIFIC",
              "deliveryDate": "%s",
              "dishId": %d
            }
            """.formatted(LocalDate.now().plusDays(3), salmon.getId());

            given()
                .header("Authorization", lineCookToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Missing auth returns 401")
        void missingAuthReturns401()
        {
            given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Delivery date too far in future returns 400")
        void deliveryDateTooFarReturns400()
        {
            given()
                .header("Authorization", lineCookToken)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "name": "Smør",
                  "quantity": 3.0,
                  "unit": "KG",
                  "requestType": "GENERAL",
                  "deliveryDate": "%s"
                }
                """.formatted(LocalDate.now().plusDays(60)))
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("DISH_SPECIFIC without dishId returns 400")
        void dishSpecificWithoutDishIdReturns400()
        {
            given()
                .header("Authorization", lineCookToken)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "name": "Smør",
                  "quantity": 3.0,
                  "unit": "KG",
                  "requestType": "DISH_SPECIFIC",
                  "deliveryDate": "%s"
                }
                """.formatted(LocalDate.now().plusDays(3)))
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("PUT /ingredient-requests/{id}")
    class Update
    {
        @Test
        @DisplayName("Should update ingredient request")
        void update()
        {
            IngredientRequest dill = (IngredientRequest) seeded.get("req_dill");
            Dish salmon = (Dish) seeded.get("dish_salmon");

            String payload = """
                {
                  "name": "Frisk Dild (opdateret)",
                  "quantity": 11.0,
                  "unit": "BUNCH",
                  "preferredSupplier": "Inco",
                  "note": "Opdateret note",
                  "requestType": "DISH_SPECIFIC",
                  "deliveryDate": "%s",
                  "dishId": %d
                }
                """.formatted(LocalDate.now().plusDays(4), salmon.getId());

            IngredientRequestDTO response = given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(ENDPOINT_URL + "/" + dill.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(IngredientRequestDTO.class);

            assertThat(response.id(), is(dill.getId()));
            assertThat(response.name(), is("Frisk Dild (opdateret)"));
            assertThat(response.quantity(), is(11.0));
            assertThat(response.note(), is("Opdateret note"));
            assertThat(response.requestType(), is(RequestType.DISH_SPECIFIC));
            assertThat(response.deliveryDate(), is(LocalDate.now().plusDays(4)));
        }

        @Test
        @DisplayName("Cannot update approved request — returns 409")
        void cannotUpdateApprovedRequestReturns409()
        {
            given().header("Authorization", headChefToken)
                .patch(ENDPOINT_URL + "/" + reqTruffleId + "/approve");

            String payload = """
            {
              "name": "Changed",
              "quantity": 5.0,
              "unit": "KG",
              "requestType": "GENERAL_STOCK",
              "deliveryDate": "%s"
            }
            """.formatted(LocalDate.now().plusDays(2));

            given()
                .header("Authorization", lineCookToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(ENDPOINT_URL + "/" + reqTruffleId)
                .then()
                .statusCode(409);
        }
    }

    @Nested
    @DisplayName("DELETE /ingredient-requests/{id}")
    class Delete
    {
        @Test
        @DisplayName("Should delete an ingredient request")
        void delete()
        {
            IngredientRequest dill = (IngredientRequest) seeded.get("req_dill");

            given()
                .header("Authorization", lineCookToken)
                .when()
                .delete(ENDPOINT_URL + "/" + dill.getId())
                .then()
                .statusCode(204);

            given()
                .header("Authorization", lineCookToken)
                .when()
                .get(ENDPOINT_URL + "/" + dill.getId())
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Line cook cannot delete another cook's request")
        void cannotDeleteOthersRequestReturns403()
        {
            String reneToken = TestAuthenticationUtil.bearerToken("rene@cold.com", "Hash4");

            given()
                .header("Authorization", reneToken)
                .when()
                .delete(ENDPOINT_URL + "/" + reqDillId)
                .then()
                .statusCode(403);
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

