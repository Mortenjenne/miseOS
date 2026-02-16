package app.dtos;

import app.enums.RequestType;
import app.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IngredientRequestDTO(
    Long id,
    String name,
    double quantity,
    String unit,
    String preferredSupplier,
    String note,
    Status status,
    RequestType requestType,
    LocalDate deliveryDate,
    LocalDateTime createdAt,
    LocalDateTime reviewedAt,
    Long createdByUserId,
    Long dishSuggestionId
) {}
