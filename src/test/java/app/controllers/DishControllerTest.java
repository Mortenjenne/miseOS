package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.dish.AvailableDishesDTO;
import app.dtos.dish.DishCreateDTO;
import app.dtos.dish.DishOptionDTO;
import app.persistence.entities.Dish;
import app.persistence.entities.IEntity;
import app.persistence.entities.Station;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DishControllerTest
{
    private static final String ENDPOINT_URL = "/dishes";
    private static final int TEST_PORT = 7775;
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
    @DisplayName("GET /dishes/search")
    class Search
    {
        @Test
        @DisplayName("Should return matching active dish for query")
        void searchReturnsMatch()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("query", "laks")
                .when()
                .get(ENDPOINT_URL + "/search")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].nameDA", equalTo("Røget Laks"))
                .body("[0].station.id", notNullValue())
                .body("[0].allergens", notNullValue());
        }

        @Test
        @DisplayName("Should return 400 when query is missing")
        void searchMissingQueryReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/search")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 when query is blank")
        void searchBlankQueryReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("query", " ")
                .when()
                .get(ENDPOINT_URL + "/search")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /dishes/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return dish with station reference and allergens")
        void returnsDishWithCollections()
        {
            Dish salmon = (Dish) seeded.get("dish_salmon");

            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + salmon.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(salmon.getId().intValue()))
                .body("nameDA", equalTo(salmon.getNameDA()))
                .body("station.id", equalTo(salmon.getStation().getId().intValue()))
                .body("station.name", equalTo(salmon.getStation().getStationName()))
                .body("allergens", hasSize(salmon.getAllergens().size()))
                .body("allergens.id", everyItem(notNullValue()))
                .body("allergens.nameDA", everyItem(notNullValue()))
                .body("allergens.displayNumber", everyItem(greaterThan(0)));
        }

        @Test
        @DisplayName("Should return 404 for unknown id")
        void notFoundReturns404()
        {
            given()
                .header("Authorization", headChefToken)
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
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/-1")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /dishes")
    class GetAll
    {
        @Test
        @DisplayName("Should return all dishes (including inactive) when no filters are provided")
        void getAllNoFiltersReturnsAll()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(10));
        }

        @Test
        @DisplayName("Should return only active dishes when active=true")
        void getAllActiveTrueReturnsOnlyActive()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("active", true)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(9))
                .body("active", everyItem(equalTo(true)));
        }

        @Test
        @DisplayName("Should return only dishes for a station when stationId is provided")
        void getAllByStationId()
        {
            Station hot = (Station) seeded.get("station_hot");

            given()
                .header("Authorization", headChefToken)
                .queryParam("stationId", hot.getId())
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("station.id", everyItem(equalTo(hot.getId().intValue())));
        }

        @Test
        @DisplayName("Should return 400 for invalid active value")
        void getAllInvalidActiveReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("active", "sand")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /dishes/available")
    class AvailableForMenu
    {
        @Test
        @DisplayName("Should return new and previous dishes for week/year")
        void returnsAvailableDishes()
        {
            AvailableDishesDTO dto = given()
                .header("Authorization", headChefToken)
                .queryParam("week", 7)
                .queryParam("year", 2026)
                .when()
                .get(ENDPOINT_URL + "/available")
                .then()
                .statusCode(200)
                .extract()
                .as(AvailableDishesDTO.class);

            assertThat(dto.week(), is(7));
            assertThat(dto.year(), is(2026));
            assertThat(dto.thisWeekDishes(), is(notNullValue()));
            assertThat(dto.fromDishBank(), is(notNullValue()));
            assertThat(dto.thisWeekDishes().values(), hasSize(2));
            assertThat(dto.fromDishBank().values(), hasSize(2));
        }

        @Test
        @DisplayName("Should return 400 when week missing")
        void missingWeekReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("year", 2026)
                .when()
                .get(ENDPOINT_URL + "/available")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 for invalid week range")
        void invalidWeekReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("week", 60)
                .queryParam("year", 2026)
                .when()
                .get(ENDPOINT_URL + "/available")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /dishes/grouped")
    class Grouped
    {
        @Test
        @DisplayName("Should return active dishes grouped by station name")
        void returnsGrouped()
        {
            Map<String, List<DishOptionDTO>> grouped = given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/grouped")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getMap(".");

            assertThat(grouped.keySet(), hasItems("Cold Kitchen", "Hot Kitchen"));
            assertThat(grouped.get("Cold Kitchen"), is(notNullValue()));
            assertThat(grouped.get("Hot Kitchen"), is(notNullValue()));
        }
    }

    @Nested
    @DisplayName("PATCH /dishes/{id}/activate")
    class Activate
    {
        @Test
        @DisplayName("Should activate inactive dish (head chef)")
        void activatesDish()
        {
            Dish inactive = (Dish) seeded.get("dish_old");

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + inactive.getId() + "/activate")
                .then()
                .statusCode(200)
                .body("id", equalTo(inactive.getId().intValue()))
                .body("active", equalTo(true));
        }
    }

    @Nested
    @DisplayName("PATCH /dishes/{id}/deactivate")
    class Deactivate
    {
        @Test
        @DisplayName("Should deactivate active dish (head chef)")
        void deactivatesDish()
        {
            Dish dish = (Dish) seeded.get("dish_salmon");

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + dish.getId() + "/deactivate")
                .then()
                .statusCode(200)
                .body("id", equalTo(dish.getId().intValue()))
                .body("active", equalTo(false));
        }
    }

    @Nested
    @DisplayName("POST /dishes")
    class Create
    {
        @Test
        @DisplayName("Should create dish and return 201 (head chef)")
        void createsDish()
        {
            DishCreateDTO dto = new DishCreateDTO(
                "Test ret",
                "Test Beskrivelse",
                2L,
                Set.of(2L,3L)
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
                .body("nameDA", equalTo("Test ret"))
                .body("station.id", equalTo(2))
                .body("station.name", equalToIgnoringCase("Hot kitchen"))
                .body("allergens", hasSize(2));
        }

        @Test
        @DisplayName("Should return 401 when missing auth header")
        void missingAuthReturns401()
        {
            DishCreateDTO dto = new DishCreateDTO(
                "Test ret",
                "Test Beskrivelse",
                2L,
                Set.of(2L,3L)
            );

            given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("PUT /dishes/{id}")
    class Update
    {
        @Test
        @DisplayName("Should update dish")
        void updatesDish()
        {
            Dish dish = (Dish) seeded.get("dish_salmon");

            String body = """
            {
              "nameDA": "Updated Navn",
              "descriptionDA": "Updated Beskrivelse",
              "nameEN": "Updated Name",
              "descriptionEN": "Updated Description",
              "allergenIds": []
            }
            """;

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(ENDPOINT_URL + "/" + dish.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(dish.getId().intValue()))
                .body("nameDA", equalTo("Updated Navn"))
                .body("allergens", hasSize(0));
        }
    }

    @Nested
    @DisplayName("DELETE /dishes/{id}")
    class Delete
    {
        @Test
        @DisplayName("Should delete dish that is not used in a menu")
        void deletesDish()
        {
            Dish deletable = (Dish) seeded.get("dish_delete");

            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + deletable.getId())
                .then()
                .statusCode(204);

            given()
                .when()
                .header("Authorization", headChefToken)
                .get(ENDPOINT_URL + "/" + deletable.getId())
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 400 when deleting dish used in menus")
        void deleteUsedDishReturns400()
        {
            Dish used = (Dish) seeded.get("dish_roastbeef");

            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + used.getId())
                .then()
                .statusCode(409);
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
