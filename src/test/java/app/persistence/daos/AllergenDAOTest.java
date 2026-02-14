package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.persistence.entities.Allergen;
import app.persistence.entities.IEntity;
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
class AllergenDAOTest
{

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private AllergenDAO allergenDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        allergenDAO = new AllergenDAO(emf);
    }

    @AfterAll
    void tearDown()
    {
        if (emf != null) emf.close();
    }

    @Test
    @DisplayName("Create - should persist a new allergen")
    void create()
    {
        Allergen newAllergen = new Allergen("Peanuts");
        Allergen result = allergenDAO.create(newAllergen);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), is("Peanuts"));
    }

    @Test
    @DisplayName("Create - should throw exception when allergen is null")
    void createNullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> allergenDAO.create(null));
    }

    @Test
    @DisplayName("Retrieve - should return all seeded allergens")
    void getAll()
    {
        Set<Allergen> allergens = allergenDAO.getAll();
        assertThat(allergens, hasSize(greaterThanOrEqualTo(8)));
    }

    @Test
    @DisplayName("Get by ID - should return correct allergen")
    void getByID()
    {
        Allergen seed = (Allergen) seeded.get("allergen_gluten");
        Allergen fetched = allergenDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getName(), is(seed.getName()));
    }

    @Test
    @DisplayName("Get by ID - should throw EntityNotFoundException for non-existing ID")
    void getByIDNotFoundThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> allergenDAO.getByID(9999L));
    }

    @Test
    @DisplayName("Get by ID - should throw IllegalArgumentException for negative ID")
    void getByIDNegativeIdThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> allergenDAO.getByID(-5L));
    }

    @Test
    @DisplayName("Update - should change name of existing allergen")
    void update()
    {
        Allergen seed = (Allergen) seeded.get("allergen_dairy");
        seed.setName("Laktose (Mælk)");

        Allergen updated = allergenDAO.update(seed);

        assertThat(updated.getName(), is("Laktose (Mælk)"));

        Allergen fetched = allergenDAO.getByID(seed.getId());
        assertThat(fetched.getName(), is("Laktose (Mælk)"));
    }

    @Test
    @DisplayName("Update - should throw exception when updating null or transient allergen")
    void updateInvalidThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> allergenDAO.update(null));

        Allergen transientAllergen = new Allergen("Shellfish");
        assertThrows(IllegalArgumentException.class, () -> allergenDAO.update(transientAllergen));
    }

    @Test
    @DisplayName("Delete - should remove allergen and return true")
    void delete()
    {
        Allergen seed = (Allergen) seeded.get("allergen_nuts");
        Long id = seed.getId();

        boolean isDeleted = allergenDAO.delete(id);

        assertTrue(isDeleted);
        assertThrows(EntityNotFoundException.class, () -> allergenDAO.getByID(id));
    }

    @Test
    @DisplayName("Delete - should throw EntityNotFoundException when deleting non-existing ID")
    void deleteNotFoundThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> allergenDAO.delete(9999L));
    }

    @Test
    @DisplayName("Find by Name - should return allergen when name matches")
    void findByName()
    {
        Optional<Allergen> result = allergenDAO.findByName("Gluten");

        assertTrue(result.isPresent());
        assertThat(result.get().getName(), is("Gluten"));
    }

    @Test
    @DisplayName("Find by Name - should return empty Optional when name is not found")
    void findByNameNotFoundReturnsEmpty()
    {
        Optional<Allergen> result = allergenDAO.findByName("Non-existing Allergen");

        assertTrue(result.isEmpty());
    }
}
