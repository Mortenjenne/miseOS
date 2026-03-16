package app.dtos.shopping;

import app.enums.SupportedLanguage;

import java.time.LocalDate;

public record CreateShoppingListDTO(
    LocalDate deliveryDate,
    SupportedLanguage targetLanguage
)
{
}
