package app.controllers;

import app.dtos.notification.AdminNotificationSnapshotDTO;
import app.dtos.security.AuthenticatedUser;
import app.enums.SessionType;
import app.services.INotificationRegistry;
import app.services.INotificationSnapshotService;
import app.utils.SecurityUtil;
import io.javalin.http.Context;
import io.javalin.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationController implements INotificationController
{
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final INotificationRegistry notificationRegistry;
    private final INotificationSnapshotService notificationSnapshotService;

    public NotificationController(INotificationRegistry notificationRegistry, INotificationSnapshotService notificationSnapshotService)
    {
        this.notificationRegistry = notificationRegistry;
        this.notificationSnapshotService = notificationSnapshotService;
    }

    @Override
    public void handleNotifications(WsConfig ws)
    {
        ws.onConnect(this::handleConnect);
        ws.onClose(this::handleClose);
        ws.onError(this::handleError);
    }

    @Override
    public void handleConnect(WsConnectContext ctx)
    {
        ctx.enableAutomaticPings();

        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUserWebSocket(ctx);
        logger.info("WebSocket connecting: User: {} with role: {} with session: {}", authUser.userId(), authUser.userRole(), ctx.sessionId());

        if (authUser.isHeadChef() || authUser.isSousChef())
        {
            ctx.attribute("sessionType", SessionType.ADMIN.name());
            notificationRegistry.registerAdmin(ctx);
            return;
        }

        ctx.attribute("sessionType", SessionType.STAFF.name());
        notificationRegistry.registerStaff(ctx, authUser.userId());
    }

    @Override
    public void handleClose(WsCloseContext ctx)
    {
        logger.info("WebSocket closing session: {} reason: {}", ctx.sessionId(), ctx.reason());
        cleanup(ctx);
    }

    @Override
    public void handleError(WsErrorContext ctx)
    {
        logger.info("WebSocket closing: {} error: {} ", ctx.sessionId(), String.valueOf(ctx.error()));
        cleanup(ctx);

    }

    @Override
    public void getSnapshot(Context ctx)
    {
        AdminNotificationSnapshotDTO snapshot = notificationSnapshotService.getPendingSnapshot();
        ctx.status(200).json(snapshot);
    }

    private void cleanup(WsContext ctx)
    {
        String sessionType = ctx.attribute("sessionType");

        if (SessionType.ADMIN.name().equals(sessionType))
        {
            notificationRegistry.unregisterAdmin(ctx);
        }
        else if (SessionType.STAFF.name().equals(sessionType))
        {
            notificationRegistry.unregisterStaff(ctx);
        }
        else
        {
            logger.warn("Cleanup called on unregistered session: {}", ctx.sessionId());
        }
    }
}
