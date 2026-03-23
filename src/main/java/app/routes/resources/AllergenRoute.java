package app.routes.resources;

import app.controllers.IAllergenController;
import app.enums.Role;
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

    public EndpointGroup getRoutes()
    {
        return () -> path("/allergens", () ->
        {
            get("", allergenController::getAll, Role.KITCHEN_STAFF);
            get("name/{name}", allergenController::getByName, Role.KITCHEN_STAFF);
            get("{id}", allergenController::getById, Role.KITCHEN_STAFF);
            post("", allergenController::create, Role.HEAD_CHEF);
            post("seed", allergenController::seedEUAllergens, Role.HEAD_CHEF);
            put("{id}", allergenController::update, Role.HEAD_CHEF);
            delete("{id}", allergenController::delete, Role.HEAD_CHEF);
        });
    }
}
