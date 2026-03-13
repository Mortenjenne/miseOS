package app.routes;

import app.controllers.IWeeklyMenuController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class WeeklyMenuRoute
{
    private final IWeeklyMenuController weeklyMenuController;

    public WeeklyMenuRoute(IWeeklyMenuController weeklyMenuController)
    {
        this.weeklyMenuController = weeklyMenuController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("/weekly-menus", () ->
        {
            get("/", weeklyMenuController::getAll);
            get("/current", weeklyMenuController::getCurrentWeekMenu);
            get("/by-week", weeklyMenuController::getByWeekAndYear);
            post("/", weeklyMenuController::create);
            get("/{id}", weeklyMenuController::getById);
            delete("/{id}", weeklyMenuController::delete);
            post("/{id}/slots", weeklyMenuController::addMenuSlot);
            put("/{id}/slots/{slotId}", weeklyMenuController::updateMenuSlot);
            delete("/{id}/slots/{slotId}", weeklyMenuController::removeMenuSlot);
            post("/{id}/slots/{slotId}/translate", weeklyMenuController::translateSlot);
            post("/{id}/translate", weeklyMenuController::translateMenu);
            post("/{id}/publish", weeklyMenuController::publishMenu);
        });
    }
}
