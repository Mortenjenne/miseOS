package app.routes.ressources;

import app.controllers.IDishController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class DishRoute
{
    private final IDishController dishController;

    public DishRoute(IDishController dishController)
    {
        this.dishController = dishController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("dishes", () ->
        {
           get("/", dishController::getAll, Role.HEAD_CHEF, Role.SOUS_CHEF);
           get("/search", dishController::search, Role.KITCHEN_STAFF);
           get("/available", dishController::getAvailableForMenu, Role.HEAD_CHEF, Role.SOUS_CHEF);
           get("/grouped", dishController::getAllGrouped, Role.HEAD_CHEF, Role.SOUS_CHEF);
           get("/{id}", dishController::getById, Role.KITCHEN_STAFF);
           post("/", dishController::create, Role.HEAD_CHEF, Role.SOUS_CHEF);
           put("/{id}", dishController::update, Role.HEAD_CHEF, Role.SOUS_CHEF);
           delete("/{id}", dishController::delete, Role.HEAD_CHEF, Role.SOUS_CHEF);
           patch("/{id}/activate", dishController::activate, Role.HEAD_CHEF, Role.SOUS_CHEF);
           patch("/{id}/deactivate", dishController::deactivate, Role.HEAD_CHEF, Role.SOUS_CHEF);
        });
    }
}
