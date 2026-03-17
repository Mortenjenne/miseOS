package app.routes;

import app.controllers.INotificationController;
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
        };
    }
}
