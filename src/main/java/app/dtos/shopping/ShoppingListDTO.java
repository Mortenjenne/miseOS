package app.dtos.shopping;

import java.time.LocalDate;
import java.util.List;

public record ShoppingListDTO(
    Long id,
    LocalDate deliveryDate,
    String status,
    String createdBy,
    int itemCount,
    List<ShoppingListItemDTO> items,
    boolean allOrdered
)
{
}
