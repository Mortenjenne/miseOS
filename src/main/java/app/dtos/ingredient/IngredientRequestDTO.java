package app.dtos.ingredient;

import app.dtos.dish.DishReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import app.enums.RequestType;
import app.enums.Status;
import app.enums.Unit;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IngredientRequestDTO(
    Long id,
    String name,
    double quantity,
    Unit unit,
    String preferredSupplier,
    String note,
    Status status,
    RequestType requestType,
    LocalDate deliveryDate,
    UserReferenceDTO requestedBy,
    DishReferenceDTO dish,
    LocalDateTime reviewedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
