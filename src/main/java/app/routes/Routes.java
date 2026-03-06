package app.routes;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Routes
{
    public EndpointGroup getRoutes()
    {
        return () ->
        {
            get("/", ctx -> ctx.result("Welcome to miseOS!"));
        };
    }
}
