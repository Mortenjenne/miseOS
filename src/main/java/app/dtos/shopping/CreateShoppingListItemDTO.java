package app.dtos.shopping;

import app.enums.Unit;

public record CreateShoppingListItemDTO(
    String ingredientName,
    Double quantity,
    Unit unit,
    String supplier
)
{}
