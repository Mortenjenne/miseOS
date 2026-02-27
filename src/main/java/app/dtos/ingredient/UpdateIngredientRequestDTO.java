package app.dtos.ingredient;

import app.enums.RequestType;
import app.enums.Status;
import app.enums.Unit;

import java.time.LocalDate;

public record UpdateIngredientRequestDTO(
    Long ingredientId,
    Long userEditor,
    Long dishSuggestionId,
    String name,
    double quantity,
    Unit unit,
    String preferredSupplier,
    String note,
    Status status,
    RequestType requestType,
    LocalDate deliveryDate
)
{
}
