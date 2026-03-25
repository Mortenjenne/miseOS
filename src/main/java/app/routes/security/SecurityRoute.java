package app.routes.security;

import app.controllers.ISecurityController;
import app.controllers.IUserController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SecurityRoute
{
    private final ISecurityController securityController;
    private final IUserController userController;

    public SecurityRoute(ISecurityController securityController, IUserController userController)
    {
        this.securityController = securityController;
        this.userController = userController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> path("auth", () ->
        {
            get("health", securityController::healthCheck, Role.ANYONE);
            post("login", securityController::login, Role.ANYONE);
            post("register", userController::create, Role.ANYONE);
        });
    }
}
