package app.dtos.shopping;

import app.enums.Unit;

public record CreateShoppingListItemDTO(
    Long shoppingListId,
    Long userId,
    String ingredientName,
    Double quantity,
    Unit unit,
    String supplier
)
{}
