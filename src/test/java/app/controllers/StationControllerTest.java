package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.station.StationDTO;
import app.dtos.station.StationRequestDTO;
import app.mappers.StationMapper;
import app.persistence.entities.IEntity;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StationControllerTest
{
    private static final String ENDPOINT_URL = "/stations";
    private static final int TEST_PORT = 7771;
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
    }

    @Nested
    @DisplayName("GET /stations/")
    class GetAll
    {
        @Test
        @DisplayName("Should return all seeded stations")
        void returnsAll()
        {
            List<StationDTO> stations = given()
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getList(".", StationDTO.class);

            List<StationDTO> seededStations = seeded.values().stream()
                .filter(entity -> entity instanceof Station)
                .map(Station.class::cast)
                .map(StationMapper::toDTO)
                .toList();

            assertThat(stations, containsInAnyOrder(seededStations.toArray()));
        }
    }

    @Nested
    @DisplayName("GET /stations/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return correct station(Cold)")
        void returnsCorrectStationCold()
        {
            Station station = (Station) seeded.get("station_cold");

            given()
                .when()
                .get(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(200)
                .body("id", is(station.getId().intValue()))
                .body("name", is(station.getStationName()))
                .body("description", is(station.getDescription()));
        }

        @Test
        @DisplayName("Should return correct station(Hot)")
        void returnsCorrectStationHot()
        {
            Station station = (Station) seeded.get("station_hot");

            given()
                .when()
                .get(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(200)
                .body("id", is(station.getId().intValue()))
                .body("name", is(station.getStationName()))
                .body("description", is(station.getDescription()));
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
    @DisplayName("POST /stations")
    class Create
    {
        @Test
        @DisplayName("Should create station and return 201")
        void createsStation()
        {
            User headChef = (User) seeded.get("user_gordon");

            StationRequestDTO dto = new StationRequestDTO(
                "Sandwich",
                "Rugbrøds sandwich, sandwich panini og lave morgenmad"
            );

            given()
                .header(USER_HEADER, headChef.getId())
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is(dto.name()))
                .body("description", is(dto.description()));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void blankNameReturnsBadRequest()
        {
            User headChef = (User) seeded.get("user_gordon");

            StationRequestDTO dto = new StationRequestDTO(
                "",
                "Rugbrøds sandwich, sandwich panini og lave morgenmad"
            );

            given()
                .header(USER_HEADER, headChef.getId())
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 409 when station with same name already exists")
        void duplicateNameReturnsConflict()
        {
            User headChef = (User) seeded.get("user_gordon");
            Station station = (Station) seeded.get("station_pastry");

            StationRequestDTO dto = new StationRequestDTO(
                station.getStationName(),
                station.getDescription()
            );

            given()
                .header(USER_HEADER, headChef.getId())
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(409)
                .body("message", equalToIgnoringCase("Station with name 'Pastry' already exists"));
        }
    }

    @Nested
    @DisplayName("PUT /stations/{id}")
    class Update
    {
        @Test
        @DisplayName("Should update station and return updated fields")
        void updatesStationCold()
        {
            User headChef = (User) seeded.get("user_gordon");
            Station station = (Station) seeded.get("station_cold");

            StationRequestDTO dto = new StationRequestDTO(
                "Koldt parti(Updated)",
                "Opdateret beskrivelse"
            );

            given()
                .header(USER_HEADER, headChef.getId())
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(200)
                .body("id", is(station.getId().intValue()))
                .body("name", is(dto.name()))
                .body("description", is(dto.description()));
        }

        @Test
        @DisplayName("Should return 404 when updating unknown id")
        void unknownIdReturns404()
        {
            User headChef = (User) seeded.get("user_gordon");

            StationRequestDTO dto = new StationRequestDTO(
                "Koldt parti(Updated)",
                "Opdateret beskrivelse"
            );

            given()
                .header(USER_HEADER, headChef.getId())
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 400 when description is blank")
        void blankDescriptionReturnsBadRequest()
        {
            User headChef = (User) seeded.get("user_gordon");
            Station station = (Station) seeded.get("station_cold");

            StationRequestDTO dto = new StationRequestDTO(
                "Koldt parti(Updated)",
                ""
            );

            given()
                .header(USER_HEADER, headChef.getId())
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /stations/{id}")
    class Delete
    {
        @Test
        @DisplayName("Should delete station and return 204")
        void deletesStation()
        {
            User headChef = (User) seeded.get("user_gordon");
            Station station = (Station) seeded.get("station_salad");

            given()
                .header(USER_HEADER, headChef.getId())
                .when()
                .delete(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(204);

            given()
                .when()
                .get(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 403 when line cook tries to delete station")
        void lineCookCannotDelete()
        {
            User lineCook = (User) seeded.get("user_claire");
            Station station = (Station) seeded.get("station_grill");

            given()
                .header(USER_HEADER, lineCook.getId())
                .when()
                .delete(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(403)
                .body("message", equalToIgnoringCase("Only head chef or sous chef can manage stations"));
        }

        @Test
        @DisplayName("Should return 404 when deleting unknown id")
        void unknownIdReturns404()
        {
            User headChef = (User) seeded.get("user_gordon");

            given()
                .header(USER_HEADER, headChef.getId())
                .when()
                .delete(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404)
                .body("message", equalToIgnoringCase("Station with ID 9999 was not found."));
        }

        @Test
        @DisplayName("Should return 400 for negative id")
        void negativeIdReturns400()
        {
            User headChef = (User) seeded.get("user_gordon");

            given()
                .header(USER_HEADER, headChef.getId())
                .when()
                .delete(ENDPOINT_URL + "/-1")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /stations/name/{name}")
    class GetByName
    {
        @Test
        @DisplayName("Should return station matching station name")
        void returnsMatchingStation()
        {
            Station station = (Station) seeded.get("station_pastry");

            given()
                .when()
                .get(ENDPOINT_URL + "/name/pas")
                .then()
                .statusCode(200)
                .body("id", is(station.getId().intValue()))
                .body("name", is(station.getStationName()))
                .body("description", is(station.getDescription()));
        }

        @Test
        @DisplayName("Should return 404 for unknown name")
        void unknownNameReturns404()
        {
            given()
                .when()
                .get(ENDPOINT_URL + "/name/burger")
                .then()
                .statusCode(404);
        }
    }
}
