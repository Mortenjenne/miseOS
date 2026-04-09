package app.persistence.daos.impl;

import app.config.HibernateTestConfig;
import app.persistence.entities.IEntity;
import app.persistence.entities.TakeAwayOffer;
import app.persistence.entities.TakeAwayOrder;
import app.persistence.entities.User;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TakeAwayOrderDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private TakeAwayOrderDAO takeAwayOrderDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        takeAwayOrderDAO = new TakeAwayOrderDAO(emf);
    }

    @Test
    @DisplayName("Create - should persist order")
    void create()
    {
        User customer = (User) seeded.get("user_marco");

        TakeAwayOrder order = new TakeAwayOrder(customer);
        TakeAwayOrder result = takeAwayOrderDAO.create(order);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getCreatedAt(), is(LocalDate.now()));
        assertThat(result.getCustomer().getId(), is(customer.getId()));
    }

    @Test
    @DisplayName("Get by ID - should return correct order")
    void getByID()
    {
        TakeAwayOrder seed = (TakeAwayOrder) seeded.get("order_1");
        TakeAwayOrder fetched = takeAwayOrderDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getCustomer(), notNullValue());
    }

    @Test
    @DisplayName("Find by Offer ID - should return all orders for a specific offer")
    void findByOfferId()
    {
        TakeAwayOffer offer = (TakeAwayOffer) seeded.get("offer_active_today");
        Set<TakeAwayOrder> orders = takeAwayOrderDAO.findByOfferId(offer.getId());

        assertThat(orders, hasSize(2));
    }

    @Test
    @DisplayName("Sum Sold Quantity - should return sum of quantities for a date")
    void sumSoldQuantityByDate()
    {
        LocalDate today = LocalDate.now();
        Optional<Long> total = takeAwayOrderDAO.sumSoldQuantityByDate(today);

        assertTrue(total.isPresent());
        assertThat(total.get(), is(15L));
    }

    @Test
    @DisplayName("Sum Sold Quantity - should return empty or zero for date with no orders")
    void sumSoldQuantityEmptyDate() {
        LocalDate plusTenDays = LocalDate.now().plusYears(10);
        Optional<Long> total = takeAwayOrderDAO.sumSoldQuantityByDate(plusTenDays);

        assertThat(total.orElse(0L), is(0L));
    }

    @Test
    @DisplayName("Find by Date - should return all orders created today")
    void findByDate()
    {
        LocalDate today = LocalDate.now();
        Set<TakeAwayOrder> orders = takeAwayOrderDAO.findByDate(today);

        assertThat(orders, hasSize(3));
    }

    @Test
    @DisplayName("Update - should update status and quantity")
    void update()
    {
        User gordon = (User) seeded.get("user_gordon");
        TakeAwayOrder seed = (TakeAwayOrder) seeded.get("order_2");

        seed.setOrderPaid(gordon);

        TakeAwayOrder updated = takeAwayOrderDAO.update(seed);
        assertThat(updated.getId(), is(seed.getId()));
    }

    @Test
    @DisplayName("Delete - should remove order")
    void delete()
    {
        TakeAwayOrder seed = (TakeAwayOrder) seeded.get("order_3");
        boolean deleted = takeAwayOrderDAO.delete(seed.getId());

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> takeAwayOrderDAO.getByID(seed.getId()));
    }

    @Test
    @DisplayName("Validation - should throw exception for invalid order data")
    void validationTest()
    {
        assertThrows(IllegalArgumentException.class, () -> new TakeAwayOrder(null));
    }
}
