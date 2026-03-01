package app.persistence.daos.impl;

import app.config.HibernateTestConfig;
import app.persistence.entities.*;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
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
    void findAllActive()
    {
    }

    @Test
    void findByStationAndActive()
    {
    }

    @Test
    void findByOriginWeekAndYear()
    {
    }

    @Test
    void findFromPreviousWeeks()
    {
    }

    @Test
    void searchByName()
    {
    }

    @Test
    void getByIdWithAllergens()
    {
    }

    @Test
    @DisplayName("Create - should persist dish with allergens")
    void create()
    {
        Station station = (Station) seeded.get("station_hot");
        User gordon = (User) seeded.get("user_gordon");

        Allergen gluten = (Allergen) seeded.get("allergen_gluten");
        Allergen dairy = (Allergen) seeded.get("allergen_dairy");
        Set<Allergen> allergens = new HashSet<>();
        allergens.add(gluten);
        allergens.add(dairy);

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
    void getAll()
    {
    }

    @Test
    void getByID()
    {
    }

    @Test
    void update()
    {
    }

    @Test
    void delete()
    {
    }

    @Test
    void isUsedInAnyMenu()
    {
    }
}
