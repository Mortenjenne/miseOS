package app.dtos.notification;

import app.enums.NotificationCategory;
import app.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AdminNotificationMessageDTO(
    NotificationType notificationType,
    NotificationCategory category,
    int count,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime timestamp
)
{
}
