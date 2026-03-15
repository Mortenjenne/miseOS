package app.dtos.ingredient;

import app.dtos.dish.DishReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import app.enums.RequestType;
import app.enums.Status;
import app.enums.Unit;
import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate deliveryDate,
    UserReferenceDTO requestedBy,
    DishReferenceDTO dish,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime reviewedAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime updatedAt
) {}
