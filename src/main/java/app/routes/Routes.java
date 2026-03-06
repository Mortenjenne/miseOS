package app.routes;

import app.persistence.entities.Allergen;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Routes
{
    private final AllergenRoute allergenRoute;

    public Routes(AllergenRoute allergenRoute)
    {
        this.allergenRoute = allergenRoute;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            get("/", ctx -> ctx.result("Welcome to miseOS!"));
            allergenRoute.getRoutes();
        };
    }
}
