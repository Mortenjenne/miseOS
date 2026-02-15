package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.enums.RequestType;
import app.enums.Status;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.IEntity;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.User;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

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
        DishSuggestion dish = (DishSuggestion) seeded.get("dish_salmon");

        IngredientRequest request = new IngredientRequest(
            "Citroner", 5.0, "kg", "Torvehallernes grønt", "Til fisken",
            RequestType.DISH_SPECIFIC, LocalDate.now().plusDays(2), dish, user
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
    @DisplayName("Retrieve - should return all seeded requests")
    void getAll()
    {
        Set<IngredientRequest> requests = ingredientRequestDAO.getAll();
        assertThat(requests, hasSize(3));
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
        seed.setQuantity(100.0);
        seed.setNote("Updated Note");

        IngredientRequest updated = ingredientRequestDAO.update(seed);

        assertThat(updated.getQuantity(), is(100.0));
        assertThat(updated.getNote(), is("Updated Note"));
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
    @DisplayName("Find - should filter by status PENDING")
    void findByStatus()
    {
        Set<IngredientRequest> pending = ingredientRequestDAO.findByStatus(Status.PENDING);

        assertThat(pending, hasSize(greaterThanOrEqualTo(2)));
        pending.forEach(r -> assertThat(r.getRequestStatus(), is(Status.PENDING)));
    }

    @Test
    @DisplayName("Find - should return empty set for status with no matches")
    void findByStatus_EmptyResults()
    {
        Set<IngredientRequest> rejected = ingredientRequestDAO.findByStatus(Status.REJECTED);
        assertThat(rejected, is(empty()));
    }

    @Test
    @DisplayName("Find - should filter by status and delivery date")
    void findByStatusAndDeliveryDate()
    {
        IngredientRequest seed = (IngredientRequest) seeded.get("req_dill");

        Set<IngredientRequest> results = ingredientRequestDAO.findByStatusAndDeliveryDate(
            Status.PENDING,
            seed.getDeliveryDate()
        );

        assertThat(results, hasSize(1));
        assertThat(results.iterator().next().getName(), is("Frisk Dild"));
    }
}
