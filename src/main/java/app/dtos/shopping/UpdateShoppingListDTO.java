package app.dtos.shopping;

import java.time.LocalDate;

public record UpdateShoppingListDTO(
    LocalDate deliveryDate
)
{
}
