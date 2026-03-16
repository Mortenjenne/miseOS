package app.routes;

import app.controllers.IUserController;
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
                get(userController::getAll);
                get("/{id}", userController::getById);
                post("/login", userController::login);
                post( userController::create);
                put("/{id}", userController::update);
                patch("/{id}/station/{stationId}", userController::assignToStation);
                patch("/{id}/role", userController::changeRole);
                patch("/{id}/email", userController::changeEmail);
                patch("/{id}/password", userController::changePassword);
                delete("/{id}", userController::delete);
            });
        };
    }
}
