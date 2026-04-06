package app.services;

import app.persistence.entities.IngredientRequest;
import app.persistence.entities.ShoppingListItem;

import java.util.List;
import java.util.Map;

public interface IShoppingListAggregator
{
    List<String> getUniqueIngredientNames(List<IngredientRequest> ingredientRequests);

    List<ShoppingListItem> aggregateAndGetShoppingListItems(List<IngredientRequest> ingredientRequests, Map<String, String> normalizedNames);
}
