package app.dtos;

import app.enums.RequestType;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.User;

import java.time.LocalDate;

public record CreateIngredientRequestDTO(String name, double quantity, String unit, String preferredSupplier, String note, RequestType requestType, LocalDate deliveryDate, Long dishSuggestionId)
{
}
