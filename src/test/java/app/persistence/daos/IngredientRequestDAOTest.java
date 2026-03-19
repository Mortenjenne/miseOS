package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.enums.RequestType;
import app.enums.Status;
import app.enums.Unit;
import app.persistence.daos.impl.IngredientRequestDAO;
import app.persistence.entities.*;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IngredientRequestDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private IngredientRequestDAO ingredientRequestDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        ingredientRequestDAO = new IngredientRequestDAO(emf);
    }

    @Test
    @DisplayName("Create - should persist a request with dependencies")
    void create()
    {
        User user = (User) seeded.get("user_claire");
        Dish dish = (Dish) seeded.get("dish_salmon");

        IngredientRequest request = new IngredientRequest(
            "Citroner",
            5.0, Unit.KG,
            "Torvehallernes grønt",
            "Til fisken",
            RequestType.DISH_SPECIFIC,
            LocalDate.now().plusDays(2),
            dish,
            user
        );

        IngredientRequest result = ingredientRequestDAO.create(request);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getRequestStatus(), is(Status.PENDING));
        assertThat(result.getCreatedBy().getEmail(), is(user.getEmail()));
    }

    @Test
    @DisplayName("Create - should throw exception when request is null")
    void createNullRequestThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ingredientRequestDAO.create(null));
        assertTrue(exception.getMessage().contains("IngredientRequest cannot be null"));
    }

    @Test
    @DisplayName("Get by ID - should return correct request")
    void getByID()
    {
        IngredientRequest seed = (IngredientRequest) seeded.get("req_dill");
        IngredientRequest fetched = ingredientRequestDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getName(), is("Frisk Dild"));
    }

    @Test
    @DisplayName("Get by ID - should throw EntityNotFoundException for non-existing id")
    void getByID_NotFound_ThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> ingredientRequestDAO.getByID(9999L));
    }

    @Test
    @DisplayName("Update - should change quantity and notes")
    void update()
    {
        IngredientRequest seed = (IngredientRequest) seeded.get("req_flour");

        seed.update(
            seed.getName(),
            100.0,
            seed.getUnit(),
            seed.getPreferredSupplier(),
            "Updated note",
            seed.getDeliveryDate(),
            seed.getDish()
        );

        IngredientRequest updated = ingredientRequestDAO.update(seed);

        assertThat(updated.getQuantity(), is(100.0));
        assertThat(updated.getNote(), is("Updated note"));
    }

    @Test
    @DisplayName("Delete - should remove request and fail on subsequent fetch")
    void delete()
    {
        IngredientRequest seed = (IngredientRequest) seeded.get("req_truffle");
        Long id = seed.getId();

        boolean deleted = ingredientRequestDAO.delete(id);

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> ingredientRequestDAO.getByID(id));
    }

    @Test
    @DisplayName("Find by filter - should return all seeded requests")
    void getAll()
    {
        List<IngredientRequest> requests = ingredientRequestDAO.findByFilter(null, null, null, null, null);
        assertThat(requests, hasSize(19));
    }

    @Test
    @DisplayName("Find by filter - should filter by status PENDING")
    void findByStatus()
    {
        List<IngredientRequest> pending = ingredientRequestDAO.findByFilter(Status.PENDING, null, null, null, null);

        assertThat(pending, hasSize(greaterThanOrEqualTo(2)));
        pending.forEach(r -> assertThat(r.getRequestStatus(), is(Status.PENDING)));
    }

    @Test
    @DisplayName("Find by filter - should return empty set for status with no matches")
    void findByStatusEmptyResults()
    {
        List<IngredientRequest> rejected = ingredientRequestDAO.findByFilter(Status.REJECTED, null, null, null, null);
        assertThat(rejected, is(empty()));
    }

    @Test
    @DisplayName("Find by filter- should filter by status and delivery date")
    void findByStatusAndDeliveryDate()
    {
        IngredientRequest seed = (IngredientRequest) seeded.get("req_dill");

        List<IngredientRequest> results = ingredientRequestDAO.findByFilter(Status.PENDING, seed.getDeliveryDate(), null, null, null);

        assertThat(results, hasSize(1));
        assertThat(results.iterator().next().getName(), is("Frisk Dild"));
    }

    @Test
    @DisplayName("Find by filter - filter by stationId returns only requests for that station")
    void findByStationId()
    {
        Station cold = (Station) seeded.get("station_cold");

        List<IngredientRequest> results = ingredientRequestDAO.findByFilter(null, null, null, null, cold.getId());

        assertThat(results, is(not(empty())));
        results.forEach(r ->
            assertThat(r.getDish().getStation().getId(), is(cold.getId()))
        );
    }

    @Test
    @DisplayName("Find by filter- filter by requestType DISH_SPECIFIC")
    void findByRequestType()
    {
        List<IngredientRequest> results = ingredientRequestDAO.findByFilter(null, null, null, RequestType.DISH_SPECIFIC, null);

        assertThat(results, is(not(empty())));
        results.forEach(r ->
            assertThat(r.getRequestType(), is(RequestType.DISH_SPECIFIC))
        );
    }

    @Test
    @DisplayName("Find by filter - filter by creatorId returns only that user's requests")
    void findByCreatorId()
    {
        User claire = (User) seeded.get("user_claire");

        List<IngredientRequest> results = ingredientRequestDAO.findByFilter(null, null, claire.getId(), null, null);

        assertThat(results, is(not(empty())));
        results.forEach(r ->
            assertThat(r.getCreatedBy().getId(), is(claire.getId()))
        );
    }

    @Test
    @DisplayName("Find by filter - combined status and stationId filter")
    void findByStatusAndStation()
    {
        Station cold = (Station) seeded.get("station_cold");

        List<IngredientRequest> results = ingredientRequestDAO.findByFilter(Status.PENDING, null, null, null, cold.getId());

        assertThat(results, is(not(empty())));
        results.forEach(r -> {
            assertThat(r.getRequestStatus(), is(Status.PENDING));
            assertThat(r.getDish().getStation().getId(), is(cold.getId()));
        });
    }

    @Test
    @DisplayName("Find by filter - unknown stationId returns empty")
    void findByUnknownStationReturnsEmpty()
    {
        List<IngredientRequest> results = ingredientRequestDAO.findByFilter(null, null, null, null, 9999L);

        assertThat(results, is(empty()));
    }

    @Test
    @DisplayName("Find by filter - all filters null returns all requests")
    void findAllFiltersNullReturnsAll()
    {
        List<IngredientRequest> all = ingredientRequestDAO.findByFilter(null, null, null, null, null);
        List<IngredientRequest> filtered = ingredientRequestDAO.findByFilter(Status.PENDING, null, null, null, null);

        assertThat(all.size(), greaterThan(filtered.size()));
    }

    @Test
    @DisplayName("Count pending dishes - Should return correct number of pending ingredient requests")
    void countPendingDishes()
    {
        int numberOfPendingDishes = ingredientRequestDAO.getPendingRequestCount();
        assertThat(numberOfPendingDishes, is(3));
    }
}
