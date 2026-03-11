package app.routes;

import app.controllers.IDishController;
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
        return () -> path("dishes", () -> {
           get("/", dishController::getAll);
           get("/search", dishController::search);
           get("/available", dishController::getAvailableForMenu);
           get("/grouped", dishController::getAllGrouped);
           get("/{id}", dishController::getById);
           post("/", dishController::create);
           put("/{id}", dishController::update);
           delete("/{id}", dishController::delete);
           patch("/{id}/activate", dishController::activate);
            patch("/{id}/deactivate", dishController::deactivate);
        });
    }
}
