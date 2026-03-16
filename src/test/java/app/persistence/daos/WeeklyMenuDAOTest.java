package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.dtos.menu.WeeklyMenuOverviewDTO;
import app.enums.MenuStatus;
import app.persistence.daos.impl.WeeklyMenuDAO;
import app.persistence.entities.*;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        WeeklyMenu menu = new WeeklyMenu(12, 2026);
        Station hot = (Station) seeded.get("station_hot");
        Dish dishSuggestion = (Dish) seeded.get("dish_salmon");

        WeeklyMenuSlot slot = new WeeklyMenuSlot(app.enums.DayOfWeek.MONDAY, dishSuggestion, hot);
        menu.addMenuSlot(slot);

        WeeklyMenu result = weeklyMenuDAO.create(menu);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getWeekNumber(), is(12));
        assertThat(result.getYear(), is(2026));
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
        WeeklyMenu seed = (WeeklyMenu) seeded.get("menu_full");
        WeeklyMenu fetched = weeklyMenuDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getWeekNumber(), is(7));
        assertThat(fetched.getYear(), is(2026));
        assertThat(fetched.getMenuStatus(), is(MenuStatus.PUBLISHED));
        assertThat(fetched.getWeeklyMenuSlots(), hasSize(8));

        List<String> fetchedDishNames = fetched.getWeeklyMenuSlots()
            .stream()
            .map(WeeklyMenuSlot::getDish)
            .filter(java.util.Objects::nonNull)
            .map(Dish::getNameDA)
            .toList();

        assertThat(fetchedDishNames, containsInAnyOrder(
            "Røget Laks",
            "Bøf Bearnaise",
            "Roastbeef",
            "Stegt Flæsk",
            "Caesar Salad",
            "Grillet Kylling",
            "Chokolademousse",
            "Tarteletter"
        ));

        List<String> fetchedStationNames = fetched.getWeeklyMenuSlots()
            .stream()
            .map(WeeklyMenuSlot::getStation)
            .filter(java.util.Objects::nonNull)
            .map(Station::getStationName)
            .toList();

        assertThat(fetchedStationNames, hasItems("Cold Kitchen", "Hot Kitchen", "Pastry"));
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
        WeeklyMenu seed = (WeeklyMenu) seeded.get("menu_draft");
        seed.setMenuStatus(MenuStatus.PUBLISHED);

        WeeklyMenu updated = weeklyMenuDAO.update(seed);
        assertThat(updated.getMenuStatus(), is(MenuStatus.PUBLISHED));
    }

    @Test
    @DisplayName("Update - should throw exception when updating non-persisted menu")
    void update_NoId_ThrowsException()
    {
        WeeklyMenu transientMenu = new WeeklyMenu(1, 2026);
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.update(transientMenu));
    }

    @Test
    @DisplayName("Delete - should remove menu and return true")
    void delete()
    {
        WeeklyMenu seed = (WeeklyMenu) seeded.get("menu_draft");
        boolean deleted = weeklyMenuDAO.delete(seed.getId());

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> weeklyMenuDAO.getByID(seed.getId()));
    }

    @Test
    @DisplayName("Find by filter - should filter by MenuStatus")
    void findByStatus()
    {
        List<WeeklyMenuOverviewDTO> publishedMenus = weeklyMenuDAO.findByFilter(MenuStatus.PUBLISHED, null, null);

        assertThat(publishedMenus, is(not(empty())));
        assertThat(publishedMenus.stream().allMatch(m -> m.menuStatus() == MenuStatus.PUBLISHED), is(true));
    }

    @Test
    @DisplayName("Find by filter - null filter should return all menus")
    void findByFilterAll()
    {
        List<WeeklyMenuOverviewDTO> allMenus = weeklyMenuDAO.findByFilter(null, null, null);

        assertThat(allMenus, hasSize(greaterThanOrEqualTo(4)));
    }

    @Test
    @DisplayName("Find by filter - should filter by week and year")
    void findByFilterWeekAndYear()
    {
        List<WeeklyMenuOverviewDTO> menus = weeklyMenuDAO.findByFilter(null, 2026, 7);

        assertThat(menus, hasSize(1));
        assertThat(menus.get(0).weekNumber(), is(7));
        assertThat(menus.get(0).year(), is(2026));
    }

    @Test
    @DisplayName("Find by week and year - should return correct menu")
    void findByWeekAndYear()
    {
        Optional<WeeklyMenu> menu = weeklyMenuDAO.findByWeekAndYear(7, 2026, null);

        assertTrue(menu.isPresent());
        assertThat(menu.get().getWeekNumber(), is(7));
        assertThat(menu.get().getYear(), is(2026));
    }

    @Test
    @DisplayName("Find by week and year - should return empty Optional if not found")
    void findByWeekAndYearNotFound()
    {
        Optional<WeeklyMenu> menu = weeklyMenuDAO.findByWeekAndYear(52, 2026, null);
        assertTrue(menu.isEmpty());
    }

    @Test
    @DisplayName("Find by week and year - should apply status filter")
    void findByWeekAndYearWithStatus()
    {
        Optional<WeeklyMenu> published = weeklyMenuDAO.findByWeekAndYear(7, 2026, MenuStatus.PUBLISHED);
        Optional<WeeklyMenu> draft = weeklyMenuDAO.findByWeekAndYear(7, 2026, MenuStatus.DRAFT);

        assertTrue(published.isPresent());
        assertTrue(draft.isEmpty());
    }

    @Test
    @DisplayName("Find by week and year - should throw for invalid ranges")
    void findByWeekAndYearInvalidRangeThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.findByWeekAndYear(60, 2026, null));
        assertThrows(IllegalArgumentException.class, () -> weeklyMenuDAO.findByWeekAndYear(10, 1990, null));
    }
}
