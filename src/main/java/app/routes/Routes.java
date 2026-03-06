package app.routes;

import app.persistence.entities.Allergen;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes
{
    private final AllergenRoute allergenRoute;
    private final UserRoute userRoute;

    public Routes(AllergenRoute allergenRoute, UserRoute userRoute)
    {
        this.allergenRoute = allergenRoute;
        this.userRoute = userRoute;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            get("/", ctx -> ctx.result("Welcome to miseOS!"));
            allergenRoute.getRoutes().addEndpoints();
            userRoute.getRoutes().addEndpoints();
        };
    }
}
