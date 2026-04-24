package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.persistence.entities.Dish;
import app.persistence.entities.IEntity;
import app.persistence.entities.TakeAwayOffer;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TakeAwayOfferControllerTest
{
    private static final String ENDPOINT_URL  = "/takeaway/offers";
    private static final int TEST_PORT = 7773;
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
    @DisplayName("GET /takeaway/offers")
    class GetAll
    {
        @Test
        @DisplayName("Should return all offers for anyone")
        void getAll()
        {
            given()
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(3)));
        }
    }

    @Nested
    @DisplayName("GET /takeaway/offers/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return correct offer")
        void returnsCorrectOffer()
        {
            TakeAwayOffer activeOffer = (TakeAwayOffer) seeded.get("offer_active_today");

            given()
                .when()
                .get(ENDPOINT_URL + "/" + activeOffer.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(activeOffer.getId().intValue()))
                .body("price", equalTo(45.0f));
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
    }

    @Nested
    @DisplayName("POST /takeaway/offers")
    class Create
    {
        @Test
        @DisplayName("Head Chef should create offer successfully")
        void createOffer()
        {
            Dish roastedPork = (Dish) seeded.get("dish_roasted_pork");

            String body = """
                {
                  "dishId": %d,
                  "offeredPortions": 30,
                  "price": 85.50
                }
                """.formatted(roastedPork.getId());

            LocalTime now = LocalTime.now();
            if (now.isBefore(LocalTime.of(12, 0)))
            {
                given()
                    .header("Authorization", headChefToken)
                    .body(body)
                    .when()
                    .post(ENDPOINT_URL)
                    .then()
                    .statusCode(409);
            } else
            {
                given()
                    .header("Authorization", headChefToken)
                    .body(body)
                    .when()
                    .post(ENDPOINT_URL)
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("enabled", equalTo(true))
                    .body("soldOut", equalTo(false))
                    .body("offeredPortions", equalTo(30))
                    .body("price", equalTo(85.5f));
            }
        }

        @Test
        @DisplayName("Customer should get 403 when trying to create")
        void customerCannotCreate()
        {
            given()
                .header("Authorization", customerToken)
                .body("{}")
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("PUT /takeaway/offers/{id}")
    class Update
    {
        @Test
        @DisplayName("Head Chef should update offer")
        void updateOffer()
        {
            TakeAwayOffer activeOffer = (TakeAwayOffer) seeded.get("offer_active_today");
            Dish salmon = (Dish) seeded.get("dish_salmon");

            String body = """
            {
              "dishId": %d,
              "offeredPortions": 50,
              "price": 50.00
            }
            """.formatted(salmon.getId());

            given()
                .header("Authorization", headChefToken)
                .body(body)
                .when()
                .put(ENDPOINT_URL + "/" + activeOffer.getId())
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("enabled", equalTo(true))
                .body("soldOut", equalTo(false))
                .body("offeredPortions", equalTo(50))
                .body("price", equalTo(50.0f));
        }
    }

    @Nested
    @DisplayName("PATCH /takeaway/offers/{id}/enable")
    class EnableOffer
    {
        @Test
        @DisplayName("Should enable disabled offer")
        void enableOffer()
        {
            TakeAwayOffer disabledOffer = (TakeAwayOffer) seeded.get("offer_disabled_today");

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + disabledOffer.getId() + "/enable")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("enabled", equalTo(true))
                .body("soldOut", equalTo(false));
        }
    }

    @Nested
    @DisplayName("PATCH /takeaway/offers/{id}/disable")
    class DisableOffer
    {
        @Test
        @DisplayName("Should disable active offer")
        void disableOffer()
        {
            TakeAwayOffer activeOffer = (TakeAwayOffer) seeded.get("offer_active_today");

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + activeOffer.getId() + "/disable")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("enabled", equalTo(false))
                .body("soldOut", equalTo(false));
        }
    }

    @Nested
    @DisplayName("DELETE /takeaway/offers/{id}")
    class Delete
    {
        @Test
        @DisplayName("Head chef can delete offer")
        void deleteOffer()
        {
            TakeAwayOffer offer = (TakeAwayOffer) seeded.get("offer_soldout_today");

            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + offer.getId())
                .then()
                .statusCode(204);

            given()
                .when()
                .get(ENDPOINT_URL + "/" + offer.getId())
                .then()
                .statusCode(404);
        }
    }
}
