package app.dtos.shopping;

import app.enums.Unit;

public record UpdateShoppingListItemDTO(
    Double quantity,
    Unit unit,
    String supplier
)
{
}
