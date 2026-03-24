package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.impl.DishSuggestionDAO;
import app.persistence.entities.*;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.*;

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

        Allergen gluten = (Allergen) seeded.get("allergen_gluten");
        Allergen milk = (Allergen) seeded.get("allergen_milk");
        Allergen eggs = (Allergen) seeded.get("allergen_eggs");

        Set<Allergen> allergens = new HashSet<>();
        allergens.add(gluten);
        allergens.add(milk);
        allergens.add(eggs);

        DishSuggestion dish = new DishSuggestion("Pasta Carbonara", "Classic pasta", 7, 2026, station, gordon, allergens);

        Allergen fish = (Allergen) seeded.get("allergen_fish");
        dish.addAllergen(fish);

        DishSuggestion result = dishSuggestionDAO.create(dish);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getNameDA(), is("Pasta Carbonara"));
        assertThat(result.getTargetWeek(), is(7));
        assertThat(result.getTargetYear(), is(2026));
        assertThat(result.getAllergens(), hasSize(4));
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
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByFilter(null, null,null, null, null, null);
        assertThat(dishes, hasSize((5)));
    }

    @Test
    @DisplayName("Get by ID - should return correct dish")
    void getByID()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("suggestion_steak");
        DishSuggestion fetched = dishSuggestionDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getNameDA(), is(seed.getNameDA()));
        assertThat(fetched.getDescriptionDA(), is(seed.getDescriptionDA()));
        assertThat(fetched.getDishStatus(), is(seed.getDishStatus()));
        assertThat(fetched.getStation(), is(seed.getStation()));
        assertThat(fetched.getAllergens(), is(notNullValue()));
        assertNotNull(fetched.getCreatedAt());
    }

    @Test
    @DisplayName("Get by ID - should throw EntityNotFoundException for missing id")
    void getByIDNotFoundThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> dishSuggestionDAO.getByID(9999L));
    }

    @Test
    @DisplayName("Get by ID - should throw IllegalArgumentException for negative id")
    void getByIDNegativeIdThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.getByID(-1L));
    }

    @Test
    @DisplayName("Update - should update status and description")
    void update()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("suggestion_salmon");
        seed.updateContent(
            "Torsk",
            "Updated Description",
            seed.getAllergens()
        );

        DishSuggestion updated = dishSuggestionDAO.update(seed);

        assertThat(updated.getNameDA(), is("Torsk"));
        assertThat(updated.getDescriptionDA(), is("Updated Description"));
    }

    @Test
    @DisplayName("Update - should update suggestion status to approved")
    void updateWithHeadChef()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("suggestion_salmon");
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
        DishSuggestion seed = (DishSuggestion) seeded.get("suggestion_salmon");
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
    void updateTransientThrowsException()
    {
        Station station = (Station) seeded.get("station_hot");
        User marco = (User) seeded.get("user_marco");
        DishSuggestion transientDish = new DishSuggestion("New Dish", "Test", 7, 2026, station, marco, new HashSet<>());

        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.update(transientDish));
    }

    @Test
    @DisplayName("Delete - should remove dish and return true")
    void delete()
    {
        DishSuggestion seed = (DishSuggestion) seeded.get("suggestion_sushi");
        boolean deleted = dishSuggestionDAO.delete(seed.getId());

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> dishSuggestionDAO.getByID(seed.getId()));
    }

    @Test
    @DisplayName("Delete - should throw EntityNotFoundException for missing id")
    void deleteNotFoundThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> dishSuggestionDAO.delete(9999L));
    }

    @Test
    @DisplayName("Delete - should throw IllegalArgumentException for negative id")
    void deleteNegativeIdThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishSuggestionDAO.delete(-1L));
    }

    @Test
    @DisplayName("Filter: Status Only - should return only PENDING suggestions")
    void filterStatusOnly()
    {
        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(Status.PENDING, null, null, null, null, null);

        assertThat(result, not(empty()));
        result.forEach(ds -> assertThat(ds.getDishStatus(), is(Status.PENDING)));
    }

    @Test
    @DisplayName("Filter: Week and Year - should return suggestions for specific period")
    void filterWeekAndYear()
    {
        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(null, null, 7, 2026, null, null);

        assertThat(result, not(empty()));
        result.forEach(ds -> {
            assertThat(ds.getTargetWeek(), is(7));
            assertThat(ds.getTargetYear(), is(2026));
        });
    }

    @Test
    @DisplayName("Filter: Station Only - should return suggestions for specific station")
    void filterStationOnly()
    {
        Station hotStation = (Station) seeded.get("station_hot");
        Long stationId = hotStation.getId();

        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(null, null, null, null, stationId, null);

        assertThat(result, not(empty()));
        result.forEach(ds -> assertThat(ds.getStation().getId(), is(stationId)));
    }

    @Test
    @DisplayName("Filter: Multiple choice - Status, Week, Year and Station")
    void filterMultiple()
    {
        Station hotStation = (Station) seeded.get("station_hot");

        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(Status.PENDING, null, 7, 2026, hotStation.getId(), null);

        assertThat(result, hasSize(2));

        List<String> names = result.stream()
            .map(DishSuggestion::getNameDA)
            .toList();

        assertThat(names, containsInAnyOrder("Bøf Bearnaise", "Tarteletter"));
        result.forEach(ds -> assertThat(ds.getDishStatus(), is(Status.PENDING)));
    }

    @Test
    @DisplayName("Filter: Empty Results - should return empty set when no match exists")
    void filterNoMatchReturnsEmpty()
    {
        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(Status.APPROVED, null,52, 2099, null, null);
        assertThat(result, is(empty()));
    }

    @Test
    @DisplayName("Filter - Find suggestions for specific week (Week 8)")
    void filterSpecificWeekReturnsOnlyWeek8()
    {
        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(null, null, 8, 2026, null, null);

        assertThat(result, hasSize(1));
        assertThat(result.iterator().next().getNameDA(), is("Sushi"));
    }

    @Test
    @DisplayName("Filter - Combined Station and Week")
    void filterStationAndWeekReturnsMatch()
    {
        Station hot = (Station) seeded.get("station_hot");

        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(null, null,  7, 2026, hot.getId(), null);

        assertThat(result, hasSize(2));
        result.forEach(ds -> assertThat(ds.getStation().getId(), is(hot.getId())));
    }

    @Test
    @DisplayName("Filter - Sorting by CreatedAt (Newest first)")
    void filterOrderByCreatedAtReturnsSorted()
    {
        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(null, null, null, null, null, "createdAt");

        assertThat(result, hasSize(5));
        List<DishSuggestion> list = new ArrayList<>(result);
        assertFalse(list.get(0).getCreatedAt().isAfter(list.get(4).getCreatedAt()));
    }

    @Test
    @DisplayName("Filter - Search for non-existent station")
    void filterNonExistentStationReturnsEmpty()
    {
        Set<DishSuggestion> result = dishSuggestionDAO.findByFilter(null, null,7, 2026, 999L, null);
        assertThat(result, is(empty()));
    }

    @Test
    @DisplayName("Count pending dishes - Should return correct number of pending dish suggestions")
    void countPendingDishes()
    {
        int numberOfPendingDishes = dishSuggestionDAO.getPendingSuggestionsCount();
        assertThat(numberOfPendingDishes, is(5));
    }
}
