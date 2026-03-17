package app.dtos.notification;

import app.enums.NotificationCategory;
import app.enums.NotificationType;

public record AdminNotificationMessageDTO(
    NotificationType notificationType,
    NotificationCategory category,
    int count
)
{
}
