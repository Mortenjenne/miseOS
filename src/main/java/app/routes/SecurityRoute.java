package app.routes;

import app.controllers.ISecurityController;
import app.controllers.IUserController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class SecurityRoute
{
    private final ISecurityController securityController;
    private final IUserController userController;

    public SecurityRoute(ISecurityController securityController, IUserController userController)
    {
        this.securityController = securityController;
        this.userController = userController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("auth", () ->
        {
            post("/login", securityController::login, Role.ANYONE);
            post("/register", userController::create, Role.ANYONE);
        });
    }


}
