package app.dtos.takeaway;

import app.dtos.user.UserReferenceDTO;
import app.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TakeAwayOrderDTO(
    Long id,
    int totalOrderLines,
    UserReferenceDTO customer,
    List<TakeAwayOrderLineDTO> orderLines,
    double totalOrderPrice,
    OrderStatus orderStatus,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime orderedAt,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createdAt
)
{
}
