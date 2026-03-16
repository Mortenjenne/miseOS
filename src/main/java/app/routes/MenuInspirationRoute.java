package app.routes;

import app.controllers.IMenuInspirationController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MenuInspirationRoute
{
    private final IMenuInspirationController menuInspirationController;

    public MenuInspirationRoute(IMenuInspirationController menuInspirationController)
    {
        this.menuInspirationController = menuInspirationController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("/menu-inspirations", () ->
        {
            get("/daily", menuInspirationController::getDailyInspiration);
            sse("/stream", menuInspirationController::getStreamingSuggestions);
        });
    }
}
