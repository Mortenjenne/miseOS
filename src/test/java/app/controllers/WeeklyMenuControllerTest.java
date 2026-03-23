package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.menu.MenuDishDTO;
import app.dtos.menu.WeeklyMenuDTO;
import app.dtos.menu.WeeklyMenuSlotDTO;
import app.enums.DayOfWeek;
import app.persistence.entities.IEntity;
import app.persistence.entities.WeeklyMenu;
import app.persistence.entities.WeeklyMenuSlot;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WeeklyMenuControllerTest
{
    private static final String ENDPOINT_URL = "/weekly-menus";
    private static final int TEST_PORT = 7776;
    private static EntityManagerFactory emf;
    private static Javalin app;

    private Map<String, IEntity> seeded;
    private String headChefToken;
    private String sousChefToken;
    private String lineChefToken;
    private long fullMenuId;
    private long draftMenuId;
    private long slotsMenuId;
    private long hotStationId;
    private long coldStationId;
    private long boeufDishId;
    private long salmonDishId;
    private long slotIdFromSlotMenu;

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
    void resetAndPopulateDatabase()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();

        seeded = populator.getSeededData();
        headChefToken = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");
        lineChefToken = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
        sousChefToken = TestAuthenticationUtil.bearerToken("marco@grill.com", "Hash3");
        fullMenuId = seeded.get("menu_full").getId();
        draftMenuId = seeded.get("menu_draft").getId();
        slotsMenuId = seeded.get("menu_slots").getId();
        hotStationId = seeded.get("station_hot").getId();
        coldStationId = seeded.get("station_cold").getId();
        boeufDishId = seeded.get("dish_boeuf").getId();
        salmonDishId = seeded.get("dish_salmon").getId();

        WeeklyMenu slotMenu = (WeeklyMenu) seeded.get("menu_slots");
        slotIdFromSlotMenu = slotMenu.getWeeklyMenuSlots().stream()
            .findFirst()
            .map(WeeklyMenuSlot::getId)
            .orElseThrow();
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Nested
    @DisplayName("POST /weekly-menus")
    class Create
    {
        @Test
        @DisplayName("Should create weekly menu as head chef and return menu payload")
        void createAsHeadChef()
        {
            String body = """
            {
              "week": 12,
              "year": 2026
            }
            """;

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(201)
                .body("menuId", notNullValue())
                .body("weekNumber", equalTo(12))
                .body("year", equalTo(2026))
                .body("menuStatus", equalTo("DRAFT"))
                .body("menuSlots", anyOf(nullValue(), empty()));
        }

        @Test
        @DisplayName("Should return 403 for non-head/sous chef")
        void createUnauthorizedRole()
        {
            String body = """
            {
              "week": 12,
              "year": 2026
            }
            """;

            given()
                .header("Authorization", lineChefToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Should return 409 when creating a menu for a week that already exists")
        void duplicateWeekConflict()
        {
            String body = """
        {
          "week": 7,
          "year": 2026
        }
        """;

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(409)
                .body("message", containsString("already exists"));
        }
    }

    @Nested
    @DisplayName("GET /weekly-menus/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return menu with slots, station references and dishes")
        void getById()
        {
            WeeklyMenuDTO response = given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + fullMenuId)
                .then()
                .statusCode(200)
                .body("menuId", equalTo((int) fullMenuId))
                .body("menuSlots", not(empty()))
                .extract()
                .as(WeeklyMenuDTO.class);

            assertNotNull(response.menuSlots());
            assertEquals(response.numberOfSlots(), response.menuSlots().size());

            Set<String> dishNames = response.menuSlots().stream()
                .map(WeeklyMenuSlotDTO::menuDish)
                .filter(java.util.Objects::nonNull)
                .map(MenuDishDTO::nameDA)
                .collect(Collectors.toSet());

            assertTrue(dishNames.containsAll(Set.of(
                "Røget Laks", "Bøf Bearnaise", "Roastbeef", "Stegt Flæsk",
                "Caesar Salad", "Grillet Kylling", "Chokolademousse", "Tarteletter"
            )));
        }

        @Test
        @DisplayName("Should return 404 for unknown menu id")
        void getByIdNotFound()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/999999")
                .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /weekly-menus/by-week")
    class GetByWeekAndYear
    {
        @Test
        @DisplayName("Should return menu by week/year")
        void getByWeekAndYear()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("week", 7)
                .queryParam("year", 2026)
                .when()
                .get(ENDPOINT_URL + "/by-week")
                .then()
                .statusCode(200)
                .body("weekNumber", equalTo(7))
                .body("year", equalTo(2026))
                .body("menuId", notNullValue())
                .body("menuSlots", hasSize(8));
        }

        @Test
        @DisplayName("Should work without any Authorization header for guests when menu is published")
        void byWeekIsPublic() {
            given()
                .queryParam("week", 7)
                .queryParam("year", 2026)
                .when()
                .get(ENDPOINT_URL + "/by-week")
                .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("Should return 400 when week/year are missing")
        void getByWeekAndYearMissingParams()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/by-week")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /weekly-menus/current")
    class Current
    {
        @Test
        @DisplayName("Should return 200 with current published menu or 404 when none exists")
        void getCurrentWeekMenu()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/current")
                .then()
                .statusCode(anyOf(is(200), is(404)));
        }

        @Test
        @DisplayName("Should work without any Authorization header for public viewing")
        void currentMenuIsPublic() {
            given()
                .when()
                .get(ENDPOINT_URL + "/current")
                .then()
                .statusCode(anyOf(is(200), is(404)));
        }
    }

    @Nested
    @DisplayName("GET /weekly-menus")
    class GetAll
    {
        @Test
        @DisplayName("Should return overview list")
        void getAllOverview()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].id", notNullValue())
                .body("[0].weekNumber", notNullValue())
                .body("[0].year", notNullValue())
                .body("[0].menuStatus", notNullValue())
                .body("[0].slotCount", notNullValue());
        }

        @Test
        @DisplayName("Should return filtered overviews by status/year/week")
        void getAllWithFilters()
        {
            given()
                .header("Authorization", sousChefToken)
                .queryParam("status", "PUBLISHED")
                .queryParam("year", 2026)
                .queryParam("week", 7)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].menuStatus", equalTo("PUBLISHED"))
                .body("[0].weekNumber", equalTo(7))
                .body("[0].year", equalTo(2026));
        }

        @Test
        @DisplayName("Should return 403 for line cook")
        void getAllUnauthorized()
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
    @DisplayName("POST /weekly-menus/{id}/slots")
    class AddSlot
    {
        @Test
        @DisplayName("Should add slot to draft menu and return updated menu")
        void addMenuSlot()
        {
            String body = """
            {
              "dayOfWeek": "FRIDAY",
              "stationId": %d,
              "dishId": %d
            }
            """.formatted(hotStationId, boeufDishId);

            WeeklyMenuDTO response = given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL + "/" + draftMenuId + "/slots")
                .then()
                .statusCode(201)
                .body("menuId", equalTo((int) draftMenuId))
                .extract()
                .as(WeeklyMenuDTO.class);

            assertTrue(response.menuSlots().stream().anyMatch(s ->
                s.dayOfWeek() == DayOfWeek.FRIDAY &&
                    s.station() != null &&
                    s.station().id().equals(hotStationId) &&
                    s.menuDish() != null &&
                    s.menuDish().id().equals(boeufDishId)
            ));
        }

        @Test
        @DisplayName("Should return 400 for station/dish mismatch")
        void addMenuSlotStationMismatch()
        {
            String body = """
            {
              "dayOfWeek": "FRIDAY",
              "stationId": %d,
              "dishId": %d
            }
            """.formatted(coldStationId, boeufDishId);

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ENDPOINT_URL + "/" + draftMenuId + "/slots")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("PUT /weekly-menus/{id}/slots/{slotId}")
    class UpdateSlot
    {
        @Test
        @DisplayName("Should update slot dish and return updated menu")
        void updateMenuSlot()
        {
            String body = """
            {
              "dishId": %d
            }
            """.formatted(salmonDishId);

            WeeklyMenuDTO response = given()
                .header("Authorization", sousChefToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(ENDPOINT_URL + "/" + slotsMenuId + "/slots/" + slotIdFromSlotMenu)
                .then()
                .statusCode(200)
                .body("menuId", equalTo((int) slotsMenuId))
                .extract()
                .as(WeeklyMenuDTO.class);

            WeeklyMenuSlotDTO updatedSlot = response.menuSlots().stream()
                .filter(s -> s.menuSlotId().equals(slotIdFromSlotMenu))
                .findFirst()
                .orElseThrow();

            assertNotNull(updatedSlot.menuDish());
            assertEquals(salmonDishId, updatedSlot.menuDish().id());
            assertNotNull(updatedSlot.menuDish().nameDA());
        }
    }

    @Nested
    @DisplayName("DELETE /weekly-menus/{id}/slots/{slotId}")
    class RemoveSlot
    {
        @Test
        @DisplayName("Should remove slot and return menu with one less slot")
        void removeMenuSlotFromMenu()
        {
            WeeklyMenuDTO before = given()
                .header("Authorization", sousChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + slotsMenuId)
                .then()
                .statusCode(200)
                .extract()
                .as(WeeklyMenuDTO.class);

            int beforeSize = before.menuSlots().size();

            WeeklyMenuDTO after = given()
                .header("Authorization", sousChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + slotsMenuId + "/slots/" + slotIdFromSlotMenu)
                .then()
                .statusCode(200)
                .extract()
                .as(WeeklyMenuDTO.class);

            assertEquals(beforeSize - 1, after.menuSlots().size());
            assertTrue(after.menuSlots().stream().noneMatch(s -> s.menuSlotId().equals(slotIdFromSlotMenu)));
        }
    }

    @Nested
    @DisplayName("POST /weekly-menus/{id}/publish")
    class Publish
    {
        @Test
        @DisplayName("Should return 409 when menu contains untranslated dishes")
        void publishMenuUntranslatedReturnsConflict()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .post(ENDPOINT_URL + "/" + fullMenuId + "/publish")
                .then()
                .statusCode(409)
                .body("message", containsString("Cannot publish - untranslated dishes"));
        }

        @Test
        @DisplayName("Should return 403 for line cook publishing")
        void publishUnauthorizedUser()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .post(ENDPOINT_URL + "/" + fullMenuId + "/publish")
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("DELETE /weekly-menus/{id}")
    class Delete
    {
        @Test
        @DisplayName("Should delete draft menu and return 404 on next request: GET")
        void deleteAndNotFoundAfter()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + draftMenuId)
                .then()
                .statusCode(204);

            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + draftMenuId)
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 403 for line cook delete")
        void deleteUnauthorizedUser()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + draftMenuId)
                .then()
                .statusCode(403);
        }
    }
}
