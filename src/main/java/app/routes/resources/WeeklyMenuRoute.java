package app.routes.resources;

import app.controllers.IWeeklyMenuController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class WeeklyMenuRoute
{
    private final IWeeklyMenuController weeklyMenuController;

    public WeeklyMenuRoute(IWeeklyMenuController weeklyMenuController)
    {
        this.weeklyMenuController = weeklyMenuController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> path("/weekly-menus", () ->
        {
            get("", weeklyMenuController::getAll, Role.HEAD_CHEF, Role.SOUS_CHEF);
            get("current", weeklyMenuController::getCurrentWeekMenu, Role.ANYONE);
            get("by-week", weeklyMenuController::getByWeekAndYear, Role.KITCHEN_STAFF, Role.ANYONE);
            post("", weeklyMenuController::create, Role.HEAD_CHEF, Role.SOUS_CHEF);
            get("{id}", weeklyMenuController::getById, Role.HEAD_CHEF, Role.SOUS_CHEF);
            delete("{id}", weeklyMenuController::delete, Role.HEAD_CHEF, Role.SOUS_CHEF);
            post("{id}/slots", weeklyMenuController::addMenuSlot, Role.HEAD_CHEF, Role.SOUS_CHEF);
            put("{id}/slots/{slotId}", weeklyMenuController::updateMenuSlot, Role.HEAD_CHEF, Role.SOUS_CHEF);
            delete("{id}/slots/{slotId}", weeklyMenuController::removeMenuSlot, Role.HEAD_CHEF, Role.SOUS_CHEF);
            post("{id}/slots/{slotId}/translate", weeklyMenuController::translateSlot, Role.HEAD_CHEF, Role.SOUS_CHEF);
            post("{id}/translate", weeklyMenuController::translateMenu, Role.HEAD_CHEF, Role.SOUS_CHEF);
            post("{id}/publish", weeklyMenuController::publishMenu, Role.HEAD_CHEF, Role.SOUS_CHEF);
        });
    }
}
