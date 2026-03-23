package app.services.impl;

import app.enums.RequestType;
import app.enums.Unit;
import app.enums.UserRole;
import app.persistence.entities.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShoppingListAggregatorTest
{
    private final ShoppingListAggregator shoppingListAggregator = new ShoppingListAggregator();

    private List<IngredientRequest> ingredientRequests;

    private User headChef;
    private User lineCook;
    private Dish salmonDish;

    @BeforeAll
    void setUp()
    {
        Station coldStation = new Station("Cold Kitchen", "Salads & Starters");

        headChef = new User("Gordon", "Ramsay", "gordon@kitchen.com", "hashed", UserRole.HEAD_CHEF);
        lineCook = new User("Claire", "Smyth", "claire@pastry.com", "hashed", UserRole.LINE_COOK);

        salmonDish = new Dish(
            "Røget Laks",
            "Laks med dildcreme og rugbrødschips",
            coldStation,
            null,
            headChef,
            7,
            2026
        );

        LocalDate deliveryDate = LocalDate.now().plusDays(3);

        ingredientRequests = List.of(
            new IngredientRequest(
                "Onion", 5.0, Unit.KG, "Inco", "Løg til sauce",
                RequestType.GENERAL_STOCK, deliveryDate, null, lineCook
            ),
            new IngredientRequest(
                "Onion", 3.0, Unit.KG, "Inco", "Løg til salat",
                RequestType.GENERAL_STOCK, deliveryDate, null, lineCook
            ),
            new IngredientRequest(
                "Garlic", 10.0, Unit.PCS, "Inco", "Til marinade",
                RequestType.GENERAL_STOCK, deliveryDate, null, lineCook
            ),
            new IngredientRequest(
                "Garlic", 5.0, Unit.PCS, "Inco", "Ekstra hvidløg",
                RequestType.GENERAL_STOCK, deliveryDate, null, lineCook
            ),
            new IngredientRequest(
                "Frisk dild", 10.0, Unit.BUNCH, "Grønttorvet", "Til laks",
                RequestType.DISH_SPECIFIC, deliveryDate, salmonDish, lineCook
            ),
            new IngredientRequest(
                "Smør", 2.0, Unit.KG, "Arla", "Sauce base",
                RequestType.GENERAL_STOCK, deliveryDate, null, headChef
            )
        );
    }

    @Test
    void getUniqueIngredientNames()
    {
        List<String> uniqueIngredientNames = shoppingListAggregator.getUniqueIngredientNames(ingredientRequests);

        assertNotNull(uniqueIngredientNames);
        assertThat(uniqueIngredientNames, containsInAnyOrder("Onion", "Garlic", "Frisk dild", "Smør"));
    }

    @Test
    void aggregateAndGetShoppingListItems()
    {
        Map<String, String> normalized = Map.of(
            "Løg", "Onion",
            "Hvidløg", "Garlic",
            "Frisk dild", "Frisk dild",
            "Smør", "Smør"
        );

        List<ShoppingListItem> items = shoppingListAggregator.aggregateAndGetShoppingListItems(ingredientRequests, normalized);

        assertThat(items, notNullValue());
        assertThat(items, hasSize(4));
        assertThat(items.get(0).getIngredientName(), is("Løg"));
    }

    @Test
    void getIngredientsGrouped()
    {
    }

    @Test
    void formatNotes()
    {
    }

    @Test
    void calculateTotal()
    {
        List<IngredientRequest> onionRequests = ingredientRequests.stream()
            .filter(request -> request.getName().equalsIgnoreCase("Onion"))
            .toList();

        Double totalQuantity = shoppingListAggregator.calculateTotal(onionRequests);

        assertEquals(8.0, totalQuantity);
    }

    @Test
    void getMostCommonSupplier()
    {
        String mostCommonSuplier = shoppingListAggregator.getMostCommonSupplier(ingredientRequests);

        assertEquals("Inco", mostCommonSuplier);

    }
}
