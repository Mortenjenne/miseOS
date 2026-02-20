package app.dtos.ingredient;

import app.enums.RequestType;

import java.time.LocalDate;

public record CreateIngredientRequestDTO(String name, double quantity, String unit, String preferredSupplier, String note, RequestType requestType, LocalDate deliveryDate, Long dishSuggestionId)
{
}
