package app.dtos.shopping;

import app.dtos.user.UserReferenceDTO;
import app.enums.ShoppingListStatus;
import app.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ShoppingListDTO(
    Long id,
    LocalDate deliveryDate,
    ShoppingListStatus status,
    UserReferenceDTO createdBy,
    int itemCount,
    List<ShoppingListItemDTO> items,
    boolean allOrdered,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime finalizedAt
)
{
}
