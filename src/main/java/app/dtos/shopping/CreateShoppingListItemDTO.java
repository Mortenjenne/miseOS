package app.dtos.shopping;

import app.enums.Unit;

public record CreateShoppingListItemDTO(
    String ingredientName,
    double quantity,
    Unit unit,
    String supplier
)
{}
