package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.entities.*;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DishSuggestionDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private DishSuggestionDAO dishSuggestionDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        dishSuggestionDAO = new DishSuggestionDAO(emf);
    }

    @Test
    @DisplayName("Create - should persist dish and check cascade with allergens")
    void create()
    {
        Station station = (Station) seeded.get("station_hot");
        User gordon = (User) seeded.get("user_gordon");

        DishSuggestion dish = new DishSuggestion("Pasta Carbonara", "Classic pasta", station, gordon);

        Allergen lactose = (Allergen) seeded.get("allergen_lactose");
        dish.addAllergen(lactose);

        DishSuggestion result = dishSuggestionDAO.create(dish);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getNameDA(), is("Pasta Carbonara"));
    }

    @Test
    @DisplayName("Create - should throw exception when dish is null")
    void createNullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.create(null));
    }

    @Test
    @DisplayName("Get All - should retrieve all seeded dishes")
    void getAll()
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.getAll();
        assertThat(dishes, hasSize((5)));
    }

    @Test
    @DisplayName("Get by ID - should return correct dish")
    void getByID()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("dish_steak");
        DishSuggestion fetched = dishSuggestionDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getNameDA(), is(seed.getNameDA()));
        assertThat(fetched.getDescriptionDA(), is(seed.getDescriptionDA()));
        assertThat(fetched.getDishStatus(), is(seed.getDishStatus()));
        assertThat(fetched.getStation(), is(seed.getStation()));
        assertNotNull(fetched.getCreatedAt());
    }

    @Test
    @DisplayName("Get by ID - should throw EntityNotFoundException for missing id")
    void getByID_NotFound_ThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> dishSuggestionDAO.getByID(9999L));
    }

    @Test
    @DisplayName("Get by ID - should throw IllegalArgumentException for negative id")
    void getByID_NegativeId_ThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.getByID(-1L));
    }


    @Test
    @DisplayName("Get with Allergens - should fetch dish and lazy-loaded allergens")
    void getByIdWithAllergens() {
        DishSuggestion seed = (DishSuggestion) seeded.get("dish_steak");

        Optional<DishSuggestion> fetched = dishSuggestionDAO.getByIdWithAllergens(seed.getId());

        assertTrue(fetched.isPresent());
        assertThat(fetched.get().getAllergens(), is(notNullValue()));
    }

    @Test
    @DisplayName("Get with Allergens - should return empty Optional for missing id")
    void getByIdWithAllergensNotFoundReturnsEmpty()
    {
        // Bemærk: getByIdWithAllergens returnerer Optional, så den bør ikke kaste exception ved "not found"
        // (medmindre du specifikt kaster det i din DAO). Standard er at returnere Optional.empty().
        Optional<DishSuggestion> fetched = dishSuggestionDAO.getByIdWithAllergens(9999L);
        assertTrue(fetched.isEmpty());
    }

    @Test
    @DisplayName("Get with Allergens - should throw IllegalArgumentException for negative id")
    void getByIdWithAllergensNegativeIdThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.getByIdWithAllergens(-1L));
    }

    @Test
    @DisplayName("Update - should update status and description")
    void update()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("dish_salmon");
        seed.setNameDA("Torsk");
        seed.setDescriptionDA("Updated Description");

        DishSuggestion updated = dishSuggestionDAO.update(seed);

        assertThat(updated.getNameDA(), is("Torsk"));
        assertThat(updated.getDescriptionDA(), is("Updated Description"));
    }

    @Test
    @DisplayName("Update - should update suggestion status to approved")
    void updateWithHeadChef()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("dish_salmon");
        User headChef = (User) seeded.get("user_gordon");
        seed.approve(headChef);

        DishSuggestion updated = dishSuggestionDAO.update(seed);

        assertThat(updated.getDishStatus(), is(Status.APPROVED));
        assertThat(updated.getReviewedBy(), is(headChef));
        assertThat(updated.getReviewedAt(), is(notNullValue()));
        assertTrue(LocalDateTime.now().isAfter(updated.getReviewedAt()));
    }

    @Test
    @DisplayName("Update - should throw UnauthorizedActionException when user is not head chef")
    void updateWithLineCookThrowsException()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("dish_salmon");
        User lineCook = (User) seeded.get("user_claire");

        assertThrows(UnauthorizedActionException.class, () -> seed.approve(lineCook));
    }

    @Test
    @DisplayName("Update - should throw exception when updating null")
    void updateNullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.update(null));
    }

    @Test
    @DisplayName("Update - should throw exception when updating transient entity (no ID)")
    void update_Transient_ThrowsException()
    {
        Station station = (Station) seeded.get("station_hot");
        User marco = (User) seeded.get("user_marco");
        DishSuggestion transientDish = new DishSuggestion("New Dish", "Test", station, marco);

        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.update(transientDish));
    }

    @Test
    @DisplayName("Delete - should remove dish and return true")
    void delete()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("dish_sushi");
        boolean deleted = dishSuggestionDAO.delete(seed.getId());

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> dishSuggestionDAO.getByID(seed.getId()));
    }

    @Test
    @DisplayName("Delete - should throw EntityNotFoundException for missing id")
    void delete_NotFound_ThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> dishSuggestionDAO.delete(9999L));
    }

    @Test
    @DisplayName("Delete - should throw IllegalArgumentException for negative id")
    void delete_NegativeId_ThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.delete(-1L));
    }


    @Test
    @DisplayName("Find by Status - should return matching dishes")
    void findByStatus()
    {
        Set<DishSuggestion> pending = dishSuggestionDAO.findByStatus(Status.PENDING);
        assertThat(pending, notNullValue());
        assertThat(pending, hasSize((5)));
    }

    @Test
    @DisplayName("Find by Status - should throw exception for null status")
    void findByStatusNullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.findByStatus(null));
    }

    @Test
    @DisplayName("Find by Status - should return empty set for status with no dishes")
    void findByStatusEmptyResult()
    {
        Set<DishSuggestion> rejected = dishSuggestionDAO.findByStatus(Status.REJECTED);
        assertThat(rejected, is(empty()));
    }

    @Test
    @DisplayName("Find by Station and Status - should return matching dishes")
    void findByStationAndStatus()
    {
        Station hot = (Station) seeded.get("station_hot");
        Set<DishSuggestion> results = dishSuggestionDAO.findByStationAndStatus(hot.getId(), Status.PENDING);
        assertThat(results, notNullValue());
        assertThat(results, hasSize(2));
    }

    @Test
    @DisplayName("Find by Station and Status - should throw exception for null status")
    void findByStationAndStatusNullStatusThrowsException()
    {
        Station hot = (Station) seeded.get("station_hot");
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.findByStationAndStatus(hot.getId(), null));
    }

    @Test
    @DisplayName("Find by Station and Status - should throw exception for negative station id")
    void findByStationAndStatus_NegativeId_ThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.findByStationAndStatus(-1L, Status.PENDING));
    }

    @Test
    @DisplayName("Find by Week and Year - should return matching dishes")
    void findByWeekAndYear()
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByWeekAndYear(7, 2025);
        assertThat(dishes, notNullValue());
        assertThat(dishes, hasSize(4));
        assertThat(dishes, containsInAnyOrder(
            seeded.get("dish_salmon"),
            seeded.get("dish_steak"),
            seeded.get("dish_tartelet"),
            seeded.get("dish_roastbeef")
        ));
    }

    @Test
    @DisplayName("Find by Week and Year - should return empty set for empty week")
    void findByWeekAndYearNoDishesReturnsEmpty()
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByWeekAndYear(52, 2025);
        assertThat(dishes, is(empty()));
    }

    @Test
    @DisplayName("Find by Week and Year - should throw exception for invalid week")
    void findByWeekAndYearInvalidWeekThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.findByWeekAndYear(60, 2025));
    }

    @Test
    @DisplayName("Find by Week and Year - should throw exception for invalid year")
    void findByWeekAndYearInvalidYearThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.findByWeekAndYear(10, 1990));
    }
}
