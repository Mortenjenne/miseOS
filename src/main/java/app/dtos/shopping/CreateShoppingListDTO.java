package app.dtos.shopping;

import java.time.LocalDate;

public record CreateShoppingListDTO(
    LocalDate deliveryDate,
    Long userId,
    String targetLanguage
)
{
}
