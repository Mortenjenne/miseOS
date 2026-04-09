package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.persistence.entities.IEntity;
import app.persistence.entities.TakeAwayOffer;
import app.persistence.entities.TakeAwayOrder;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TakeAwayOrderControllerTest
{
    private static final String ENDPOINT_URL  = "/takeaway/orders";
    private static final int TEST_PORT = 7783;
    private static EntityManagerFactory emf;
    private static Javalin app;

    private Map<String, IEntity> seeded;
    private String headChefToken;
    private String customerToken;

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
        customerToken = TestAuthenticationUtil.bearerToken("hans@gmail.com", "Hash5");
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Nested
    @DisplayName("GET /takeaway/orders")
    class GetOrders
    {
        @Test
        @DisplayName("Customer should see own orders")
        void customerSeesOwnOrders()
        {
            given()
                .header("Authorization", customerToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(2));
        }
    }

    @Nested
    @DisplayName("GET /takeaway/orders/{id}")
    class GetById
    {
        @Test
        @DisplayName("Customer should be able to get their own specific order")
        void getOwnOrder()
        {
            TakeAwayOrder order1 = (TakeAwayOrder) seeded.get("order_1");

            given()
                .header("Authorization", customerToken)
                .when()
                .get(ENDPOINT_URL + "/" + order1.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(order1.getId().intValue()))
                .body("totalOrderLines", equalTo(order1.getTotalOrderLines()))
                .body("totalQuantity", equalTo(order1.getTotalItems()))
                .body("totalOrderPrice", is(180.0F));
        }
    }

    @Nested
    @DisplayName("POST /takeaway/orders")
    class PlaceOrder
    {
        @Test
        @DisplayName("Customer should place an order successfully")
        void placeOrder()
        {
            TakeAwayOffer activeOffer = (TakeAwayOffer) seeded.get("offer_active_today");

            String body = """
            {
              "takeAwayOrderLines": [
                {
                  "offerId": %d,
                  "quantity": 2
                }
              ]
            }
            """.formatted(activeOffer.getId());

            given()
                .header("Authorization", customerToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(201)
                .body("orderStatus", equalTo("RESERVED"))
                .body("orderLines", hasSize(1));
        }

        @Test
        @DisplayName("Should return 400 for empty order lines")
        void emptyOrderLinesReturnsBadRequest()
        {
            String body = """
            {
              "takeAwayOrderLines": []
            }
            """;

            given()
                .header("Authorization", customerToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("PATCH /takeaway/orders/{id}/pay")
    class MarkAsPaid
    {
        @Test
        @DisplayName("Head Chef should mark order as paid")
        void markPaid()
        {
            TakeAwayOrder order1 = (TakeAwayOrder) seeded.get("order_1");

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + order1.getId() + "/pay")
                .then()
                .statusCode(200)
                .body("orderStatus", equalTo("PAID"));
        }

        @Test
        @DisplayName("Should throw 403 - When customer tries to mark as paid")
        void customerCannotMarkPaid()
        {
            TakeAwayOrder order1 = (TakeAwayOrder) seeded.get("order_1");

            given()
                .header("Authorization", customerToken)
                .when()
                .patch(ENDPOINT_URL + "/" + order1.getId() + "/pay")
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("PATCH /takeaway/orders/{id}/cancel")
    class CancelOrder
    {
        @Test
        @DisplayName("Customer should be able to cancel own order")
        void cancelOwnOrder()
        {
            TakeAwayOrder order2 = (TakeAwayOrder) seeded.get("order_2");

            given()
                .header("Authorization", customerToken)
                .when()
                .patch(ENDPOINT_URL + "/" + order2.getId() + "/cancel")
                .then()
                .statusCode(200)
                .body("orderStatus", equalTo("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("GET /takeaway/orders/summary")
    class GetSummary
    {
        @Test
        @DisplayName("Head Chef should get daily summary")
        void getSummary()
        {
            LocalDate date = LocalDate.now();

            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/summary/?date=" + date)
                .then()
                .statusCode(200)
                .body("totalOrders", notNullValue())
                .body("totalSoldPortions", notNullValue())
                .body("summaryPerOffer", notNullValue())
                .body("summaryPerOffer", notNullValue());
        }

        @Test
        @DisplayName("Customer should be forbidden from seeing summary")
        void customerCannotSeeSummary()
        {
            given()
                .header("Authorization", customerToken)
                .when()
                .get(ENDPOINT_URL + "/summary")
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
