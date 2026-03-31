package app.routes.resources;

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

    public EndpointGroup getRoutes()
    {
        return () ->
            path("users", () ->
            {
                get("", userController::getAll, Role.HEAD_CHEF);
                get("me", userController::getMe, Role.KITCHEN_STAFF);
                get("{id}", userController::getById, Role.HEAD_CHEF, Role.SOUS_CHEF);
                put("{id}", userController::update, Role.KITCHEN_STAFF);
                patch("{id}/station/{stationId}", userController::assignToStation, Role.HEAD_CHEF, Role.SOUS_CHEF);
                patch("{id}/role", userController::changeRole, Role.HEAD_CHEF);
                patch("{id}/email", userController::changeEmail, Role.KITCHEN_STAFF);
                patch("{id}/password", userController::changePassword, Role.KITCHEN_STAFF);
                delete("{id}", userController::delete, Role.HEAD_CHEF);
            });
    }
}
