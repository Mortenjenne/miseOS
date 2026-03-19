package app.routes;

import app.controllers.IUserController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public class UserRoute
{
    private final IUserController userController;

    public UserRoute(IUserController userController)
    {
        this.userController = userController;
    }

    protected EndpointGroup getRoutes()
    {
        return () ->
        {
            path("users", () ->
            {
                get("/", userController::getAll, Role.HEAD_CHEF);
                get("/{id}", userController::getById, Role.HEAD_CHEF);
                post("/", userController::create, Role.ANYONE);
                put("/{id}", userController::update, Role.HEAD_CHEF, Role.SOUS_CHEF, Role.LINE_COOK);
                patch("/{id}/station/{stationId}", userController::assignToStation);
                patch("/{id}/role", userController::changeRole);
                patch("/{id}/email", userController::changeEmail);
                patch("/{id}/password", userController::changePassword);
                delete("/{id}", userController::delete);
            });
        };
    }
}
