package app.routes.resources;

import app.controllers.INotificationController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class NotificationRoute
{
    private final INotificationController notificationController;

    public NotificationRoute(INotificationController notificationController)
    {
        this.notificationController = notificationController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
          ws("/notifications", notificationController::handleNotifications);
          get("/notifications/snapshot", notificationController::getSnapshot, Role.HEAD_CHEF, Role.SOUS_CHEF);
        };
    }
}
