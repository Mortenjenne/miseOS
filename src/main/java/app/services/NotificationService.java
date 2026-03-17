package app.services;

import app.dtos.notification.AdminNotificationMessageDTO;
import app.dtos.notification.StaffNotificationMessageDTO;
import app.dtos.user.UserReferenceDTO;
import app.enums.NotificationCategory;
import app.enums.NotificationType;
import io.javalin.websocket.WsContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService implements INotificationRegistry, INotificationSender
{
    private final Set<WsContext> adminSessions = ConcurrentHashMap.newKeySet();
    private final Map<Long, WsContext> staffSessions = new ConcurrentHashMap<>();


    @Override
    public void registerAdmin(WsContext ctx)
    {
        adminSessions.add(ctx);
    }

    @Override
    public void registerStaff(WsContext ctx, Long userId)
    {
        staffSessions.put(userId, ctx);
    }

    @Override
    public void unregisterAdmin(WsContext ctx)
    {
        adminSessions.remove(ctx);
    }

    @Override
    public void unregisterStaff(WsContext ctx)
    {
        staffSessions.entrySet()
            .removeIf(entry -> entry.getValue().session.equals(ctx.session));
    }

    @Override
    public void sendPendingUpdate(NotificationType notificationType, NotificationCategory category, int count)
    {
        AdminNotificationMessageDTO message = new AdminNotificationMessageDTO(
            notificationType,
            category,
            count
        );

        adminSessions.forEach(session ->
        {
            if(session.session.isOpen())
            {
                session.send(message);
            }
        });
    }

    @Override
    public void notifyStaff(Long userId, NotificationType notificationType, NotificationCategory category, Long requestId, String itemName, UserReferenceDTO reviewedBy)
    {
        StaffNotificationMessageDTO message = new StaffNotificationMessageDTO(
            notificationType,
            category,
            requestId,
            itemName,
            reviewedBy
        );

        WsContext session = staffSessions.get(userId);
        if (session != null && session.session.isOpen())
        {
            session.send(message);
        }
    }
}
