package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.station.StationDTO;
import app.dtos.station.StationRequestDTO;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StationControllerTest
{
    private static final String ENDPOINT_URL = "/stations";
    private static final int TEST_PORT = 7771;
    private static EntityManagerFactory emf;
    private static Javalin app;

    private Map<String, IEntity> seeded;
    private String headChefToken;
    private String sousChefToken;
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
    void resetDatabase()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();

        seeded = populator.getSeededData();
        headChefToken = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");
        lineChefToken = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
        sousChefToken = TestAuthenticationUtil.bearerToken("marco@grill.com", "Hash3");
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
                .header("Authorization", sousChefToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getList(".", StationDTO.class);

            List<Long> actualIds = stations.stream()
                .map(StationDTO::id)
                .toList();

            List<Long> expectedIds = seeded.values()
                .stream()
                .filter(entity -> entity instanceof Station)
                .map(IEntity::getId)
                .toList();

            assertThat(actualIds, containsInAnyOrder(expectedIds.toArray()));
        }

        @Test
        @DisplayName("Should return 403 when line chef tries to access GET /stations")
        void lineCookCannotGetStations()
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
    @DisplayName("GET /stations/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return correct station(Cold)")
        void returnsCorrectStationCold()
        {
            Station station = (Station) seeded.get("station_cold");

            given()
                .header("Authorization", headChefToken)
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
                .header("Authorization", sousChefToken)
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
    @DisplayName("POST /stations")
    class Create
    {
        @Test
        @DisplayName("Should create station and return 201")
        void createsStation()
        {
            StationRequestDTO dto = new StationRequestDTO(
                "Sandwich",
                "Rugbrøds sandwich, sandwich panini og lave morgenmad"
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
                .body("name", is(dto.name()))
                .body("description", is(dto.description()));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void blankNameReturnsBadRequest()
        {
            StationRequestDTO dto = new StationRequestDTO(
                "",
                "Rugbrøds sandwich, sandwich panini og lave morgenmad"
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
        @DisplayName("Should return 403 when Sous chef tries to create a Station")
        void sousChefCannotCreateStation()
        {
            StationRequestDTO dto = new StationRequestDTO("Test", "Test Desc");

            given()
                .header("Authorization", sousChefToken)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Should return 409 when station with same name already exists")
        void duplicateNameReturnsConflict()
        {
            Station station = (Station) seeded.get("station_pastry");

            StationRequestDTO dto = new StationRequestDTO(
                station.getStationName(),
                station.getDescription()
            );

            given()
                .header("Authorization", headChefToken)
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
            Station station = (Station) seeded.get("station_cold");

            StationRequestDTO dto = new StationRequestDTO(
                "Koldt parti(Updated)",
                "Opdateret beskrivelse"
            );

            given()
                .header("Authorization", headChefToken)
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
            StationRequestDTO dto = new StationRequestDTO(
                "Koldt parti(Updated)",
                "Opdateret beskrivelse"
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
        @DisplayName("Should return 400 when description is blank")
        void blankDescriptionReturnsBadRequest()
        {
            Station station = (Station) seeded.get("station_cold");

            StationRequestDTO dto = new StationRequestDTO(
                "Koldt parti(Updated)",
                ""
            );

            given()
                .header("Authorization", headChefToken)
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
            Station station = (Station) seeded.get("station_salad");

            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(204);

            given()
                .when()
                .header("Authorization", headChefToken)
                .get(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 403 when line cook tries to delete station")
        void lineCookCannotDelete()
        {
            Station station = (Station) seeded.get("station_grill");

            given()
                .header("Authorization", lineChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + station.getId())
                .then()
                .statusCode(403)
                .body("message", containsString("Insufficient role. Required: [HEAD_CHEF]"));
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
                .statusCode(404)
                .body("message", equalToIgnoringCase("Station with ID 9999 was not found."));
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
    @DisplayName("GET /stations/name/{name}")
    class GetByName
    {
        @Test
        @DisplayName("Should return station matching station name")
        void returnsMatchingStation()
        {
            Station station = (Station) seeded.get("station_pastry");

            given()
                .header("Authorization", sousChefToken)
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
                .header("Authorization", sousChefToken)
                .when()
                .get(ENDPOINT_URL + "/name/burger")
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
