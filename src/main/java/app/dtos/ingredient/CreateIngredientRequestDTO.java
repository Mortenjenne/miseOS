package app.dtos.ingredient;

import app.enums.RequestType;
import app.enums.Unit;

import java.time.LocalDate;

public record CreateIngredientRequestDTO(
    String name,
    double quantity,
    Unit unit,
    String preferredSupplier,
    String note,
    RequestType requestType,
    LocalDate deliveryDate,
    Long dishSuggestionId
)
{}
