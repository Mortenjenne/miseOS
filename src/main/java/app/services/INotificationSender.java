package app.services;

import app.dtos.user.UserReferenceDTO;
import app.enums.NotificationCategory;
import app.enums.NotificationType;

public interface INotificationSender
{
    void sendPendingUpdate(NotificationType notificationType, NotificationCategory category, int count);
    void notifyStaff(Long userId, NotificationType notificationType, NotificationCategory category, Long requestId, String itemName, UserReferenceDTO reviewedBy);
}
