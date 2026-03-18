package app.controllers;

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

        String role = ctx.queryParam("role");
        String userIdParam = ctx.queryParam("userId");

        logger.info("WebSocket connecting: User: {} with role: {} with session: {}", userIdParam, role, ctx.sessionId);

        if ("HEAD_CHEF".equals(role) || "SOUS_CHEF".equals(role))
        {
            ctx.attribute("sessionType", SessionType.ADMIN.name());
            notificationRegistry.registerAdmin(ctx);
            return;
        }

        if (userIdParam != null)
        {
            ctx.attribute("sessionType", SessionType.STAFF.name());
            notificationRegistry.registerStaff(ctx, Long.parseLong(userIdParam));
            return;
        }

        logger.warn("Closing websocket session={} due to missing/invalid registration params", ctx.sessionId());
        ctx.closeSession(1008, "Invalid websocket registration");
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
            notificationRegistry.unregisterAdmin(ctx);
            notificationRegistry.unregisterStaff(ctx);
        }
    }
}
