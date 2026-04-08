package app.persistence.daos.impl;

import app.config.HibernateTestConfig;
import app.persistence.entities.Dish;
import app.persistence.entities.IEntity;
import app.persistence.entities.TakeAwayOffer;
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
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TakeAwayOfferDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private TakeAwayOfferDAO takeAwayOfferDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        takeAwayOfferDAO = new TakeAwayOfferDAO(emf);
    }

    @Test
    @DisplayName("Create - should persist offer")
    void create()
    {
        Dish dish = (Dish) seeded.get("dish_chicken");
        User gordon = (User) seeded.get("user_gordon");

        TakeAwayOffer offer = new TakeAwayOffer(20, 50.00, gordon, dish);
        TakeAwayOffer result = takeAwayOfferDAO.create(offer);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getOfferedPortions(), is(20));
        assertThat(result.getAvailablePortions(), is(20));
        assertThat(result.getPrice(), is(50.00));
        assertThat(result.isEnabled(), is(true));
        assertThat(result.isSoldOut(), is(false));
        assertThat(result.getCreatedAt(), is(LocalDate.now()));
    }

    @Test
    @DisplayName("Get by ID - should return correct seeded offer")
    void getByID()
    {
        TakeAwayOffer seed = (TakeAwayOffer) seeded.get("offer_active_today");
        TakeAwayOffer fetched = takeAwayOfferDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getDish().getId(), is(seed.getDish().getId()));
        assertThat(fetched.getOfferedPortions(), is(10));
    }

    @Test
    @DisplayName("Update - should persist changes from domain methods")
    void update()
    {
        TakeAwayOffer seed = (TakeAwayOffer) seeded.get("offer_active_today");

        seed.sellPortions(2);
        seed.disableOffer();

        TakeAwayOffer updated = takeAwayOfferDAO.update(seed);

        assertThat(updated.getAvailablePortions(), is(8));
        assertFalse(updated.isEnabled());
    }

    @Test
    @DisplayName("Update - should trigger sold out status when portions reach zero")
    void updateSoldOutLogic()
    {
        TakeAwayOffer seed = (TakeAwayOffer) seeded.get("offer_active_today");

        seed.sellPortions(10);

        TakeAwayOffer updated = takeAwayOfferDAO.update(seed);

        assertTrue(updated.isSoldOut());
        assertFalse(updated.isEnabled());
        assertThat(updated.getAvailablePortions(), is(0));
    }

    @Test
    @DisplayName("Delete - should remove offer")
    void delete()
    {
        TakeAwayOffer seed = (TakeAwayOffer) seeded.get("offer_disabled_today");
        boolean deleted = takeAwayOfferDAO.delete(seed.getId());

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> takeAwayOfferDAO.getByID(seed.getId()));
    }

    @Test
    @DisplayName("Find by filter - returns all offers when no filters applied")
    void findByFilterReturnsAll()
    {
        Set<TakeAwayOffer> all = takeAwayOfferDAO.findByFilter(null, null, null, null);
        assertThat(all, hasSize(3));
    }

    @Test
    @DisplayName("Find by filter - enabled only returns active offers")
    void findByFilterReturnsOnlyEnabled()
    {
        Set<TakeAwayOffer> enabled = takeAwayOfferDAO.findByFilter(null, null, true, null);
        assertThat(enabled, hasSize(2));
        assertTrue(enabled.stream().allMatch(TakeAwayOffer::isEnabled));
    }

    @Test
    @DisplayName("Find by filter - disabled only returns disabled offers")
    void findByFilterReturnsOnlyDisabled()
    {
        Set<TakeAwayOffer> disabled = takeAwayOfferDAO.findByFilter(null, null, false, null);
        assertThat(disabled, hasSize(1));
        assertThat(disabled.iterator().next().getDish().getNameDA(), is("Bøf Bearnaise"));
    }

    @Test
    @DisplayName("Find by filter - filter by date returns only today's offers")
    void findByFilter_byDate_returnsOnlyToday()
    {
        Set<TakeAwayOffer> today = takeAwayOfferDAO.findByFilter(LocalDate.now(), null, null, null);
        assertThat(today, hasSize(3));
    }

    @Test
    @DisplayName("Find by filter - future date returns empty")
    void findByFilter_futureDate_returnsEmpty()
    {
        Set<TakeAwayOffer> future = takeAwayOfferDAO.findByFilter(LocalDate.now().plusDays(1), null, null, null);
        assertThat(future, empty());
    }

    @Test
    @DisplayName("Find by filter - filter by dishId returns only matching offer")
    void findByFilter_byDishId_returnsSingleOffer()
    {
        Dish salmon = (Dish) seeded.get("dish_salmon");
        Set<TakeAwayOffer> result = takeAwayOfferDAO.findByFilter(null, null, null, salmon.getId());
        assertThat(result, hasSize(1));
        assertThat(result.iterator().next().getDish().getNameDA(), is("Røget Laks"));
    }

    @Test
    @DisplayName("Exists by Dish and Date - should work for seeded dish")
    void existsByDishAndDate()
    {
        Dish salmon = (Dish) seeded.get("dish_salmon");
        LocalDate today = LocalDate.now();

        boolean exists = takeAwayOfferDAO.existsByDishAndDate(salmon.getId(), today);
        assertTrue(exists);
    }

    @Test
    @DisplayName("Get by ID - should throw EntityNotFoundException for missing id")
    void getByIDNotFoundThrowsException()
    {
        assertThrows(EntityNotFoundException.class, () -> takeAwayOfferDAO.getByID(9999L));
    }

    @Test
    @DisplayName("Validation - should throw exception for invalid constructor arguments")
    void constructorValidation()
    {
        User gordon = (User) seeded.get("user_gordon");
        Dish salmon = (Dish) seeded.get("dish_salmon");

        assertThrows(IllegalArgumentException.class, () -> new TakeAwayOffer(-1, 10.00, gordon, salmon));
        assertThrows(IllegalArgumentException.class, () -> new TakeAwayOffer(10, 10.00, null, salmon));
    }
}
