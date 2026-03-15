package app.routes;

import app.controllers.IIngredientRequestController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class IngredientRequestRoute
{
    private final IIngredientRequestController ingredientRequestController;

    public IngredientRequestRoute(IIngredientRequestController iIngredientRequestController)
    {
        this.ingredientRequestController = iIngredientRequestController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("ingredient-requests", () ->
        {
            get("/", ingredientRequestController::getAll);
            post("/", ingredientRequestController::create);
            get("/{id}", ingredientRequestController::getById);
            put("/{id}", ingredientRequestController::update);
            delete("/{id}", ingredientRequestController::delete);
            patch("/{id}/approve", ingredientRequestController::approve);
            patch("/{id}/reject", ingredientRequestController::reject);
        });
    }
}
