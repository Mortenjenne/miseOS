package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.persistence.entities.IEntity;
import app.persistence.entities.Station;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StationDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private StationDAO stationDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        stationDAO = new StationDAO(emf);
    }

    @AfterAll
    void tearDown()
    {
        emf.close();
    }

    @DisplayName(("Create - should persist a station"))
    @Test
    void create()
    {
        Station newStation = new Station("Sandwich", "Preparation of sandwiches");

        Station result = stationDAO.create(newStation);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getStationName(), is("Sandwich"));
        assertThat(result.getDescription(), is("Preparation of sandwiches"));
    }

    @DisplayName("Create - should throw exception when station is null")
    @Test
    void createNullUserThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> stationDAO.create(null));

        assertTrue(exception.getMessage().contains("Station cannot be null"));
    }

    @Test
    @DisplayName("Create - should throw exception when station is duplicate")
    void create_DuplicateStation_ThrowsException() {

        Station station = (Station) seeded.get("station_cold");
        Station duplicate = new Station(station.getStationName(), "Cold kitchen");

        assertThrows(Exception.class, () -> stationDAO.create(duplicate));
    }

    @DisplayName("Retrieve - 4 stations persisted")
    @Test
    void getAll()
    {
        Set<Station> stations = stationDAO.getAll();

        assertThat(stations, hasSize(4));
        assertThat(stations, containsInAnyOrder(
            seeded.get("station_cold"),
            seeded.get("station_hot"),
            seeded.get("station_pastry"),
            seeded.get("station_grill")
        ));
    }

    @DisplayName("Get by id - Should retrieve correct station")
    @Test
    void getByID()
    {
        Station seed = (Station) seeded.get("station_hot");

        Station fetched = stationDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getStationName(), is(seed.getStationName()));
        assertThat(fetched.getDescription(), is(seed.getDescription()));
    }

    @DisplayName("Get by id - Should throw EntityNotFoundException when station doesn't exist")
    @Test
    void getByIDNotFoundThrowsException() {

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> stationDAO.getByID(999L));

        assertTrue(exception.getMessage().contains("999"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @DisplayName("Update - Should update a stations attributes")
    @Test
    void update()
    {
        Station seed = (Station) seeded.get("station_cold");
        seed.setStationName("Super Cold Kitchen");
        seed.setDescription("Updated description");

        Station updated = stationDAO.update(seed);


        assertThat(updated.getId(), is(seed.getId()));
        assertThat(updated.getStationName(), is("Super Cold Kitchen"));

        Station fetched = stationDAO.getByID(seed.getId());
        assertThat(fetched.getStationName(), is("Super Cold Kitchen"));
    }

    @DisplayName("Delete  Should delete and throw exception after deletion")
    @Test
    void delete()
    {
        Station seed = (Station) seeded.get("station_pastry");
        Long id = seed.getId();

        boolean isDeleted = stationDAO.delete(id);

        assertThat(isDeleted, is(true));
        assertThrows(EntityNotFoundException.class, () -> stationDAO.getByID(id));
    }

    @DisplayName("Find - Should find correct station")
    @Test
    void findByName()
    {
        String targetName = "Grill";

        Optional<Station> fetched = stationDAO.findByName(targetName);

        assertTrue(fetched.isPresent());
        assertThat(fetched, notNullValue());
        assertThat(fetched.get().getStationName(), is(targetName));
        assertThat(fetched.get().getDescription(), is(((Station) seeded.get("station_grill")).getDescription()));
    }

    @DisplayName("Find - Is null when not found")
    @Test
    void findByNameIsNullWhenNotFound()
    {
        String targetName = "Burger";

        Optional<Station> fetched = stationDAO.findByName(targetName);

        assertTrue(fetched.isEmpty());
    }
}
