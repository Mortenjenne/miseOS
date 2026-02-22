package app.dtos.shopping;

import app.enums.Unit;

public record ShoppingListItemDTO(
    Long id,
    String ingredientName,
    double quantity,
    Unit unit,
    String supplier,
    String notes,
    boolean isOrdered
)
{
}
