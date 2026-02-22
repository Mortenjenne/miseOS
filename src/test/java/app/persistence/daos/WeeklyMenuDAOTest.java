package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.enums.MenuStatus;
import app.persistence.entities.*;
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
class WeeklyMenuDAOTest
{

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private WeeklyMenuDAO weeklyMenuDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        weeklyMenuDAO = new WeeklyMenuDAO(emf);
    }

    @Test
    @DisplayName("Create - should persist weekly menu and its slots")
    void create() {
        WeeklyMenu menu = new WeeklyMenu(12, 2025);
        Station hot = (Station) seeded.get("station_hot");
        DishSuggestion dishSuggestion = (DishSuggestion) seeded.get("dish_salmon");

        WeeklyMenuSlot slot = new WeeklyMenuSlot(app.enums.DayOfWeek.MONDAY, dishSuggestion, hot);
        menu.addMenuSlot(slot);

        WeeklyMenu result = weeklyMenuDAO.create(menu);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getWeekNumber(), is(12));
        assertThat(result.getMenuStatus(), is(MenuStatus.DRAFT));
        assertThat(result.getWeeklyMenuSlots(), hasSize(1));
    }

    @Test
    @DisplayName("Create - should throw exception when menu is null")
    void createNullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.create(null));
    }

    @Test
    @DisplayName("Create - should throw exception when week number and year is off")
    void createWrongWeekOrYearThrowsException()
    {
        WeeklyMenu weeklyMenu = new WeeklyMenu(0, 1900);
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.create(weeklyMenu));
    }

    @Test
    @DisplayName("Get by ID - should retrieve menu and slots")
    void getByID()
    {
        WeeklyMenu seed = (WeeklyMenu) seeded.get("menu_week7");
        WeeklyMenu fetched = weeklyMenuDAO.getByIdWithSlots(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getWeeklyMenuSlots(), hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("Get by ID with slots - should throw EntityNotFoundException and IllegalArgumentException")
    void getByIdWithSlotsNotFoundThrowsException() {

        assertThrows(EntityNotFoundException.class, () -> weeklyMenuDAO.getByIdWithSlots(9999L));
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.getByIdWithSlots(-1L));
    }

    @Test
    @DisplayName("Get by ID - should throw EntityNotFoundException for negative or non-existing ID")
    void getByIDBadIdThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.getByID(-1L));
        assertThrows(EntityNotFoundException.class, () -> weeklyMenuDAO.getByID(9999L));
    }

    @Test
    @DisplayName("Update - should change status and save")
    void update()
    {
        WeeklyMenu seed = (WeeklyMenu) seeded.get("menu_week7");
        seed.setMenuStatus(MenuStatus.PUBLISHED);

        WeeklyMenu updated = weeklyMenuDAO.update(seed);
        assertThat(updated.getMenuStatus(), is(MenuStatus.PUBLISHED));
    }

    @Test
    @DisplayName("Update - should throw exception when updating non-persisted menu")
    void update_NoId_ThrowsException()
    {
        WeeklyMenu transientMenu = new WeeklyMenu(1, 2025);
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.update(transientMenu));
    }

    @Test
    @DisplayName("Delete - should remove menu and return true")
    void delete()
    {
        WeeklyMenu seed = (WeeklyMenu) seeded.get("menu_week7");
        boolean deleted = weeklyMenuDAO.delete(seed.getId());

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> weeklyMenuDAO.getByID(seed.getId()));
    }

    @Test
    @DisplayName("Find - should filter by MenuStatus")
    void findByStatus()
    {
        Set<WeeklyMenu> draftMenus = weeklyMenuDAO.findByStatus(MenuStatus.DRAFT);
        assertThat(draftMenus, hasSize(1));
    }

    @Test
    @DisplayName("Find - should throw exception for null status")
    void findByStatus_Null_ThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.findByStatus(null));
    }

    @Test
    @DisplayName("Find by Week and Year - should return correct menu")
    void findByWeekAndYear()
    {
        Optional<WeeklyMenu> menu = weeklyMenuDAO.findByWeekAndYear(7, 2025);
        assertTrue(menu.isPresent());
        assertThat(menu.get().getWeekNumber(), is(7));
    }

    @Test
    @DisplayName("Find by Week and Year - should return empty Optional if not found")
    void findByWeekAndYear_NotFound()
    {
        Optional<WeeklyMenu> menu = weeklyMenuDAO.findByWeekAndYear(52, 2025);
        assertTrue(menu.isEmpty());
    }

    @Test
    @DisplayName("Find by Week and Year - should throw exception for invalid ranges")
    void findByWeekAndYear_InvalidRange_ThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.findByWeekAndYear(60, 2025));
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.findByWeekAndYear(10, 1990));
    }
}
