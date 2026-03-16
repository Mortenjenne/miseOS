package app.dtos.shopping;

import app.enums.Unit;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ShoppingListItemDTO(
    Long id,
    String ingredientName,
    double quantity,
    Unit unit,
    String supplier,
    String notes,
    boolean ordered,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime updatedAt
)
{
}
