package app.controllers;

import app.services.INotificationRegistry;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationController implements INotificationController
{
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final INotificationRegistry notificationRegistry;

    public NotificationController(INotificationRegistry notificationRegistry)
    {
        this.notificationRegistry = notificationRegistry;
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

        logger.info("WebSocket connecting: User {} with role {}", userIdParam, role);

        if ("HEAD_CHEF".equals(role) || "SOUS_CHEF".equals(role))
        {
            notificationRegistry.registerAdmin(ctx);
        }
        else if (userIdParam != null)
        {
            notificationRegistry.registerStaff(ctx, Long.parseLong(userIdParam));
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx)
    {
        logger.info("WebSocket closing session: {} reason: {}", ctx.sessionId(), ctx.reason());
        notificationRegistry.unregisterAdmin(ctx);
        notificationRegistry.unregisterStaff(ctx);
    }

    @Override
    public void handleError(WsErrorContext ctx)
    {
        logger.info("WebSocket closing: {} error: {} ", ctx.sessionId(), String.valueOf(ctx.error()));
        String sessionType = ctx.attribute("sessionType");

        if ("ADMIN".equals(sessionType))
        {
            notificationRegistry.unregisterAdmin(ctx);
        }
        else
        {
            notificationRegistry.unregisterStaff(ctx);
        }
    }
}
