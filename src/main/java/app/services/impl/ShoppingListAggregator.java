package app.services.impl;

import app.dtos.shopping.AggregationKey;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.ShoppingListItem;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ShoppingListAggregator implements IShoppingListAggregator
{
    private static final String DEFAULT_SUPPLIER = "AB Catering";

    @Override
    public List<String> getUniqueIngredientNames(List<IngredientRequest> ingredientRequests)
    {
        return ingredientRequests.stream()
            .map(IngredientRequest::getName)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    @Override
    public List<ShoppingListItem> aggregateAndGetShoppingListItems(List<IngredientRequest> ingredientRequests, Map<String, String> normalizedNames)
    {
        Map<AggregationKey, List<IngredientRequest>> groupedRequests = getIngredientsGrouped(ingredientRequests, normalizedNames);

        return groupedRequests.entrySet().stream()
            .map(entry ->
            {
                AggregationKey key = entry.getKey();
                List<IngredientRequest> ingredientGroup = entry.getValue();

                Double totalQuantity = calculateTotal(ingredientGroup);
                String supplier = getMostCommonSupplier(ingredientGroup);
                String notes = formatNotes(ingredientGroup);

                return new ShoppingListItem(
                    key.normalizedName(),
                    totalQuantity,
                    key.unit(),
                    supplier,
                    notes
                );
            })
            .collect(Collectors.toList());
    }

    public Map<AggregationKey, List<IngredientRequest>> getIngredientsGrouped(List<IngredientRequest> approved, Map<String, String> normalizedNames)
    {
        return approved.stream()
            .collect(Collectors.groupingBy(req -> new AggregationKey(normalizedNames.getOrDefault(req.getName(), req.getName()), req.getUnit())));
    }

    public String formatNotes(List<IngredientRequest> ingredientRequests)
    {
        return ingredientRequests.stream()
            .map(req -> String.format("%s (%s: %s %s)",
                req.getCreatedBy().getFirstName(),
                req.getName(),
                req.getQuantity(),
                req.getUnit()))
            .collect(Collectors.joining(" | "));
    }

    public Double calculateTotal(List<IngredientRequest> ingredientRequests)
    {
        return ingredientRequests.stream()
            .map(IngredientRequest::getQuantity)
            .reduce(0.0, Double::sum);
    }

    public String getMostCommonSupplier(List<IngredientRequest> requests)
    {
        return requests.stream()
            .map(IngredientRequest::getPreferredSupplier)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(DEFAULT_SUPPLIER);
    }
}
