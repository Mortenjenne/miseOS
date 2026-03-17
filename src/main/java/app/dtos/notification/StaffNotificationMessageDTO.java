package app.dtos.notification;

import app.dtos.user.UserReferenceDTO;
import app.enums.NotificationCategory;
import app.enums.NotificationType;

public record StaffNotificationMessageDTO(
    NotificationType notificationType,
    NotificationCategory category,
    Long requestId,
    String itemName,
    UserReferenceDTO reviewedBy
)
{
}
