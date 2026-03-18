package app.dtos.notification;

import app.dtos.user.UserReferenceDTO;
import app.enums.NotificationCategory;
import app.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record StaffNotificationMessageDTO(
    NotificationType notificationType,
    NotificationCategory category,
    Long requestId,
    String itemName,
    UserReferenceDTO reviewedBy,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime timestamp
)
{
}
