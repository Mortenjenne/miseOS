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

        TakeAwayOffer offer = new TakeAwayOffer(20, gordon, dish);
        TakeAwayOffer result = takeAwayOfferDAO.create(offer);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getOfferedPortions(), is(20));
        assertThat(result.getAvailablePortions(), is(20));
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

        seed.sellPortions(5);
        seed.disableOffer();

        TakeAwayOffer updated = takeAwayOfferDAO.update(seed);

        assertThat(updated.getAvailablePortions(), is(5));
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
    @DisplayName("Find Active Offers - should exclude disabled and sold out offers")
    void findActiveOffers()
    {
        LocalDate today = LocalDate.now();
        Set<TakeAwayOffer> activeOffers = takeAwayOfferDAO.findActiveOffers(today);

        assertThat(activeOffers, hasSize(1));
        assertThat(activeOffers.iterator().next().getDish().getNameDA(), is("Røget Laks"));
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

        assertThrows(IllegalArgumentException.class, () -> new TakeAwayOffer(-1, gordon, salmon));
        assertThrows(IllegalArgumentException.class, () -> new TakeAwayOffer(10, null, salmon));
    }
}
