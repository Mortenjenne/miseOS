package app.dtos.shopping;

import app.enums.Unit;

public record UpdateShoppingListItemDTO(
    Long itemId,
    Long shoppingListId,
    Long userId,
    Double quantity,
    Unit unit,
    String supplier
)
{
}
