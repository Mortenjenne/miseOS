package app.persistence.daos.impl;

import app.config.HibernateTestConfig;
import app.persistence.entities.*;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DishDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private DishDAO dishDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        dishDAO = new DishDAO(emf);
    }

    @Test
    @DisplayName("Filter: Find all active - should return only active dishes")
    void findAllActive()
    {
        Set<Dish> active = dishDAO.findByFilter(null, true);

        assertThat(active, hasSize(9));
        assertTrue(active.stream().allMatch(Dish::isActive));
    }

    @Test
    @DisplayName("Filter: Find by station - should return active dishes for station")
    void findByStationAndActive()
    {
        Station hot = (Station) seeded.get("station_hot");

        Set<Dish> dishes = dishDAO.findByFilter(hot.getId(), true);

        assertThat(dishes, hasSize(5));
        assertTrue(dishes.stream().allMatch(Dish::isActive));
        assertTrue(dishes.stream().allMatch(d -> d.getStation().getId().equals(hot.getId())));
    }

    @Test
    @DisplayName("Filter: Find by station - should return empty set for station with no active dishes")
    void findByStationAndActivesReturnsEmpty()
    {
        Station grill = (Station) seeded.get("station_grill");

        Set<Dish> dishes = dishDAO.findByFilter(grill.getId(), true);

        assertThat(dishes, is(empty()));
    }

    @Test
    @DisplayName("Filter: Find by station - should throw exception for negative id")
    void findByStationAndActiveThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishDAO.findByFilter(-1L, true));
    }

    @Test
    @DisplayName("Find by origin week - should return dishes approved for that week")
    void findByOriginWeekAndYear()
    {
        Set<Dish> dishes = dishDAO.findByOriginWeekAndYear(7, 2026);

        assertThat(dishes, hasSize(3));
        assertThat(dishes, containsInAnyOrder(
            seeded.get("dish_salmon"),
            seeded.get("dish_boeuf"),
            seeded.get("dish_tartelet")
        ));
    }

    @Test
    @DisplayName("Find by origin week - should return empty set for week with no dishes")
    void findByOriginWeekAndYearReturnsEmpty()
    {
        Set<Dish> dishes = dishDAO.findByOriginWeekAndYear(52, 2026);
        assertThat(dishes, is(empty()));
    }

    @Test
    @DisplayName("Find by origin week - should throw exception for invalid week")
    void findByOriginWeekAndYearInvalidWeekThrowsException()
    {
        assertThrows(IllegalArgumentException.class,
            () -> dishDAO.findByOriginWeekAndYear(60, 2026));
    }

    @Test
    @DisplayName("Find by origin week - should throw exception for invalid year")
    void findByOriginWeekAndYearInvalidYearThrowsException()
    {
        assertThrows(IllegalArgumentException.class,
            () -> dishDAO.findByOriginWeekAndYear(7, 1990));
    }

    @Test
    void findFromPreviousWeeks()
    {
        Set<Dish> dishes = dishDAO.findFromPreviousWeeks(7, 2026);

        assertThat(dishes, hasSize(2));
        assertThat(dishes, containsInAnyOrder(
            seeded.get("dish_roastbeef"),
            seeded.get("dish_roasted_pork")
        ));
    }

    @Test
    @DisplayName("Find from previous weeks - should return empty set for first week")
    void findFromPreviousWeeksReturnsEmpty()
    {
        Set<Dish> dishes = dishDAO.findFromPreviousWeeks(1, 2026);
        assertThat(dishes, is(empty()));
    }

    @Test
    @DisplayName("Find from previous weeks - should throw exception for invalid week")
    void findFromPreviousWeeksInvalidWeekThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishDAO.findFromPreviousWeeks(60, 2026));
    }

    @Test
    void searchByName()
    {
        Set<Dish> results = dishDAO.searchByName("Bøf");

        assertThat(results, hasSize(1));
        assertThat(results.iterator().next().getNameDA(), is("Bøf Bearnaise"));
        assertThat(results.iterator().next().getDescriptionDA(), is("Oksemørbrad med hjemmelavet bearnaise"));
    }

    @Test
    @DisplayName("Search by name - should be case insensitive")
    void searchByNameCaseInsensitive()
    {
        Set<Dish> lower = dishDAO.searchByName("laks");
        Set<Dish> upper = dishDAO.searchByName("LAKS");

        assertThat(lower, hasSize(1));
        assertThat(lower.iterator().next().getNameDA(), is("Røget Laks"));

        assertThat(upper, hasSize(1));
        assertThat(upper.iterator().next().getNameDA(), is("Røget Laks"));
    }

    @Test
    @DisplayName("Search by name - should search English name too")
    void searchByNameSearchesEnglish()
    {
        Set<Dish> results = dishDAO.searchByName("Fried");

        assertThat(results, hasSize(1));
        assertThat(results.iterator().next().getNameDA(), is("Stegt Flæsk"));
    }

    @Test
    @DisplayName("Search by name - should return empty for no match")
    void searchByNameReturnsEmpty()
    {
        Set<Dish> results = dishDAO.searchByName("XYZ_NOMATCH");
        assertThat(results, is(empty()));
    }

    @Test
    @DisplayName("Search by name - should throw exception for blank query")
    void searchByNameBlankThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishDAO.searchByName(" "));
    }

    @Test
    @DisplayName("Create - should persist dish with allergens")
    void create()
    {
        Station station = (Station) seeded.get("station_hot");
        User gordon = (User) seeded.get("user_gordon");

        Allergen gluten = (Allergen) seeded.get("allergen_gluten");
        Allergen milk = (Allergen) seeded.get("allergen_milk");
        Set<Allergen> allergens = new HashSet<>();
        allergens.add(gluten);
        allergens.add(milk);

        Dish dish = new Dish(
            "Pasta Carbonara",
            "Klassisk italiensk pasta",
            station,
            allergens,
            gordon,
            10,
            2026
        );

        Dish result = dishDAO.create(dish);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getNameDA(), is("Pasta Carbonara"));
        assertThat(result.getOriginWeek(), is(10));
        assertThat(result.getOriginYear(), is(2026));
        assertThat(result.isActive(), is(true));
        assertThat(result.getAllergens(), hasSize(2));
        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("Create - should throw exception when dish is null")
    void create_NullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishDAO.create(null));
    }

    @Test
    @DisplayName("Get all - should return all dishes including inactive")
    void getAll()
    {
        Set<Dish> dishes = dishDAO.findByFilter(null, null);
        assertThat(dishes, hasSize(10));
    }

    @Test
    @DisplayName("Get by id - should return correct dish with allergens and station")
    void getByID()
    {
        Dish seed = (Dish) seeded.get("dish_roastbeef");
        Dish fetched = dishDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getNameDA(), is(seed.getNameDA()));
        assertThat(fetched.isActive(), is(true));
        assertThat(fetched.getOriginWeek(), is(5));
        assertThat(fetched.getOriginYear(), is(2026));
        assertThat(fetched.getStation(),is(notNullValue()));
        assertThat(fetched.getStation().getStationName(), is(seed.getStation().getStationName()));
        assertThat(fetched.getAllergens(), hasSize(4));
        assertThat(fetched.getAllergens(), containsInAnyOrder(
            seeded.get("allergen_gluten"),
            seeded.get("allergen_milk"),
            seeded.get("allergen_eggs"),
            seeded.get("allergen_fish"))
        );
        assertNotNull(fetched.getCreatedAt());
    }

    @Test
    @DisplayName("Get by id - should throw EntityNotFoundException for missing id")
    void getByIDNotFoundThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> dishDAO.getByID(9999L));
    }

    @Test
    @DisplayName("Get by id - should throw IllegalArgumentException for negative id")
    void getNegativeIdThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishDAO.getByID(-1L));
    }

    @Test
    @DisplayName("Update - should update dish content")
    void update()
    {
        Dish seed = (Dish) seeded.get("dish_salmon");
        Allergen nuts = (Allergen) seeded.get("allergen_nuts");

        seed.update(
            "Grillet Laks",
            "Grillet med citron og dild",
            "Grilled Salmon",
            "Grilled with lemon and dill",
            new HashSet<>(Set.of(nuts))
        );

        Dish updated = dishDAO.update(seed);

        assertThat(updated.getNameDA(), is("Grillet Laks"));
        assertThat(updated.getNameEN(), is("Grilled Salmon"));
        assertThat(updated.getDescriptionDA(), is("Grillet med citron og dild"));
        assertThat(updated.getDescriptionEN(), is("Grilled with lemon and dill"));
        assertThat(updated.getAllergens(), hasSize(1));
    }

    @Test
    @DisplayName("Update - should throw exception when updating null")
    void updateNullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishDAO.update(null));
    }

    @Test
    @DisplayName("Deactivate - should set isActive to false")
    void deactivate()
    {
        Dish seed = (Dish) seeded.get("dish_salmon");
        assertTrue(seed.isActive());

        seed.deactivate();
        Dish updated = dishDAO.update(seed);

        assertFalse(updated.isActive());

        Set<Dish> active = dishDAO.findByFilter(null, true);
        assertThat(active, not(hasItem(updated)));
    }

    @Test
    @DisplayName("Activate - should set isActive to true")
    void activate()
    {
        Dish seed = (Dish) seeded.get("dish_old");
        assertFalse(seed.isActive());

        seed.activate();
        Dish updated = dishDAO.update(seed);

        assertTrue(updated.isActive());

        Set<Dish> active = dishDAO.findByFilter(null, true);
        assertThat(active, hasItem(updated));
    }

    @Test
    void delete()
    {
        Dish deletable = (Dish) seeded.get("dish_delete");
        boolean deleted = dishDAO.delete(deletable.getId());

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> dishDAO.getByID(deletable.getId()));
    }

    @Test
    @DisplayName("Delete - should throw EntityNotFoundException for missing id")
    void deleteNotFoundThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> dishDAO.delete(9999L));
    }

    @Test
    @DisplayName("Delete - should throw IllegalArgumentException for negative id")
    void deleteNegativeIdThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> dishDAO.delete(-1L));
    }

    @Test
    @DisplayName("Is used in menu - should return true when dish is in a menu slot")
    void isUsedInAnyMenu()
    {
        Dish laks = (Dish) seeded.get("dish_roastbeef");

        boolean used = dishDAO.isUsedInAnyMenu(laks.getId());

        assertTrue(used);
    }

    @Test
    @DisplayName("Is used in menu - should return false when dish is not in any menu")
    void isUsedInAnyMenu_ReturnsFalseWhenNotInMenu()
    {
        Dish notInMenu = (Dish) seeded.get("dish_delete");

        boolean used = dishDAO.isUsedInAnyMenu(notInMenu.getId());

        assertFalse(used);
    }
}
