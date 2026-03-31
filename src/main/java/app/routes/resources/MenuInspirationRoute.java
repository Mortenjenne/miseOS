package app.routes.resources;

import app.controllers.IMenuInspirationController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MenuInspirationRoute
{
    private final IMenuInspirationController menuInspirationController;

    public MenuInspirationRoute(IMenuInspirationController menuInspirationController)
    {
        this.menuInspirationController = menuInspirationController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> path("menu-inspirations", () ->
        {
            get("daily", menuInspirationController::getDailyInspiration, Role.KITCHEN_STAFF);
            sse("stream", menuInspirationController::getStreamingSuggestions, Role.KITCHEN_STAFF);
        });
    }
}
