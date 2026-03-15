package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.enums.ShoppingListStatus;
import app.enums.Unit;
import app.exceptions.ConflictException;
import app.persistence.daos.impl.ShoppingListDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.ShoppingList;
import app.persistence.entities.ShoppingListItem;
import app.persistence.entities.User;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShoppingListDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private ShoppingListDAO shoppingListDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        shoppingListDAO = new ShoppingListDAO(emf);
    }

    @Test
    @DisplayName("Create - should persist ShoppingList and its ShoppingListItems (Cascade Test)")
    void create()
    {
        User user = (User) seeded.get("user_gordon");
        ShoppingList newList = new ShoppingList(LocalDate.now().plusDays(2), user);

        ShoppingListItem item1 = new ShoppingListItem("Smør", 10, Unit.KG, "Arla", "Usaltet");
        ShoppingListItem item2 = new ShoppingListItem("Fløde", 5, Unit.L, "Arla", "38%");

        newList.addItem(item1);
        newList.addItem(item2);

        ShoppingList result = shoppingListDAO.create(newList);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getShoppingListItems(), hasSize(2));
    }

    @Test
    @DisplayName("Create - should throw exception when list is null")
    void createNullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> shoppingListDAO.create(null));
    }

    @Test
    @DisplayName("Get by ID - should retrieve list and its items")
    void getByID()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");

        ShoppingList fetched = shoppingListDAO.getByID(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getShoppingListItems(), hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("Get by ID - should throw exception for invalid or missing ID")
    void getByIDBadIdThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> shoppingListDAO.getByID(-1L));
        assertThrows(EntityNotFoundException.class, () -> shoppingListDAO.getByID(9999L));
    }

    @Test
    @DisplayName("Update - should change status and save")
    void update()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");

        seed.getShoppingListItems().forEach(ShoppingListItem::markAsOrdered);
        seed.finalizeShoppingList();

        ShoppingList updated = shoppingListDAO.update(seed);

        assertThat(updated.getShoppingListStatus(), is(ShoppingListStatus.FINALIZED));
        assertThat(updated.getFinalizedAt(), notNullValue());
    }

    @Test
    @DisplayName("Update - should throw ConflictException when finalizing with unordered items")
    void finalizeWithUnorderedItemsThrows()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");

        assertThrows(ConflictException.class, seed::finalizeShoppingList);
    }

    @Test
    @DisplayName("Update - should throw exception for transient entity")
    void updateTransientThrowsException()
    {
        User user = (User) seeded.get("user_gordon");
        ShoppingList transientList = new ShoppingList(LocalDate.now(), user);

        assertThrows(IllegalArgumentException.class, () -> shoppingListDAO.update(transientList));
    }

    @Test
    @DisplayName("Delete - should remove list and cascade to items")
    void delete()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");
        Long id = seed.getId();

        boolean deleted = shoppingListDAO.delete(id);

        assertTrue(deleted);
        assertThrows(EntityNotFoundException.class, () -> shoppingListDAO.getByID(id));
    }

    @Test
    @DisplayName("Delete - should throw EntityNotFoundException for unknown id")
    void deleteUnknownIdThrows()
    {
        assertThrows(EntityNotFoundException.class, () -> shoppingListDAO.delete(9999L));
    }

    @Test
    @DisplayName("Delete - should throw IllegalArgumentException for negative id")
    void deleteNegativeIdThrows()
    {
        assertThrows(IllegalArgumentException.class, () -> shoppingListDAO.delete(-1L));
    }

    @Test
    @DisplayName("Find by filter - Should only find by status draft")
    void findByStatus()
    {
        List<ShoppingList> draftLists = shoppingListDAO.findByFilter(ShoppingListStatus.DRAFT, null);
        assertThat(draftLists, hasSize(greaterThanOrEqualTo(1)));
    }


    @Test
    @DisplayName("Find by filter - should return all shopping lists")
    void getAll()
    {
        List<ShoppingList> allLists = shoppingListDAO.findByFilter(null, null);

        assertThat(allLists, hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Find by filter - should return empty set when database is empty")
    void getAll_EmptyDatabase()
    {
        TestCleanDB.truncateTables(emf);

        List<ShoppingList> allLists = shoppingListDAO.findByFilter(null, null);

        assertThat(allLists, notNullValue());
        assertThat(allLists, empty());
    }

    @Test
    @DisplayName("Find by delivery date - should return correct list")
    void findByDeliveryDate()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");

        Optional<ShoppingList> list = shoppingListDAO.findByDeliveryDate(seed.getDeliveryDate());

        assertTrue(list.isPresent());
        assertThat(list.get().getId(), is(seed.getId()));
    }

    @Test
    @DisplayName("Find by delivery date - should return empty if no matches")
    void findByDeliveryDateNotFound()
    {
        Optional<ShoppingList> list = shoppingListDAO.findByDeliveryDate(LocalDate.now().plusYears(10));
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Find by filter - filter by delivery date returns correct list")
    void findByDeliveryDateFilter()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");

        List<ShoppingList> result = shoppingListDAO.findByFilter(null, seed.getDeliveryDate());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(seed.getId()));
    }

    @Test
    @DisplayName("Find by filter - status FINALIZED returns finalized seed")
    void findByFinalizedStatusReturnsEmpty()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");

        seed.getShoppingListItems().forEach(ShoppingListItem::markAsOrdered);
        seed.finalizeShoppingList();

        shoppingListDAO.update(seed);

        List<ShoppingList> result = shoppingListDAO.findByFilter(ShoppingListStatus.FINALIZED, null);

        assertThat(result, hasSize(1));
    }

    @Test
    @DisplayName("Find by filter - combined status and date returns match")
    void findByStatusAndDate()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");

        List<ShoppingList> result = shoppingListDAO.findByFilter(ShoppingListStatus.DRAFT, seed.getDeliveryDate());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getShoppingListStatus(), is(ShoppingListStatus.DRAFT));
    }

    @Test
    @DisplayName("Find by delivery date - should throw exception for null date")
    void findByDeliveryDateNullThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> shoppingListDAO.findByDeliveryDate(null));
    }

    @Test
    @DisplayName("Cascade - should persist new item added to existing list")
    void cascadeAddItem()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");
        int originalSize = seed.getShoppingListItems().size();

        ShoppingListItem newItem = new ShoppingListItem("Sukker", 5, Unit.KG, "Danisco", "Rørsukker");
        seed.addItem(newItem);

        ShoppingList updated = shoppingListDAO.update(seed);

        assertThat(updated.getShoppingListItems(), hasSize(originalSize + 1));

        ShoppingList fetched = shoppingListDAO.getByID(seed.getId());
        assertThat(fetched.getShoppingListItems(), hasSize(originalSize + 1));
        assertTrue(fetched.getShoppingListItems().stream()
            .anyMatch(item -> item.getIngredientName().equals("Sukker")));
    }

    @Test
    @DisplayName("Cascade - should remove item via orphanRemoval")
    void cascadeRemoveItem()
    {
        ShoppingList seed = (ShoppingList) seeded.get("shopping_list_1");
        Long listId = seed.getId();

        ShoppingList managedList = shoppingListDAO.getByID(listId);
        int originalSize = managedList.getShoppingListItems().size();

        ShoppingListItem itemToRemove = managedList.getShoppingListItems().iterator().next();
        String removedName = itemToRemove.getIngredientName();
        managedList.removeItem(itemToRemove);

        ShoppingList updated = shoppingListDAO.update(managedList);

        assertThat(updated.getShoppingListItems(), hasSize(originalSize - 1));

        ShoppingList fetched = shoppingListDAO.getByID(listId);
        assertThat(fetched.getShoppingListItems(), hasSize(originalSize - 1));
        assertTrue(fetched.getShoppingListItems().stream().noneMatch(item -> item.getIngredientName().equals(removedName)));
    }
}
