package app.routes.resources;

import app.controllers.IIngredientRequestController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class IngredientRequestRoute
{
    private final IIngredientRequestController ingredientRequestController;

    public IngredientRequestRoute(IIngredientRequestController iIngredientRequestController)
    {
        this.ingredientRequestController = iIngredientRequestController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> path("ingredient-requests", () ->
        {
            get("/", ingredientRequestController::getAll, Role.KITCHEN_STAFF);
            post("/", ingredientRequestController::create, Role.KITCHEN_STAFF);
            get("/{id}", ingredientRequestController::getById, Role.KITCHEN_STAFF);
            put("/{id}", ingredientRequestController::update, Role.KITCHEN_STAFF);
            delete("/{id}", ingredientRequestController::delete, Role.KITCHEN_STAFF);
            patch("/{id}/approve", ingredientRequestController::approve, Role.HEAD_CHEF, Role.SOUS_CHEF);
            patch("/{id}/reject", ingredientRequestController::reject, Role.HEAD_CHEF, Role.SOUS_CHEF);
        });
    }
}
