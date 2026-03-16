package app.routes;

import app.controllers.IAllergenController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

public class AllergenRoute
{
    private final IAllergenController allergenController;

    public AllergenRoute(IAllergenController allergenController)
    {
        this.allergenController = allergenController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("/allergens", () ->
        {
            get("/", allergenController::getAll);
            get("/{id}", allergenController::getById);
            get("/name/{name}", allergenController::getByName);
            post("/", allergenController::create);
            post("/seed", allergenController::seedEUAllergens);
            put("/{id}", allergenController::update);
            delete("/{id}", allergenController::delete);
        });
    }
}
