package app.routes;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Routes
{
    private final AllergenRoute allergenRoute;
    private final UserRoute userRoute;
    private final StationRoute stationRoute;
    private final MenuInspirationRoute menuInspirationRoute;
    private final DishSuggestionRoute dishSuggestionRoute;

    public Routes(AllergenRoute allergenRoute, UserRoute userRoute, StationRoute stationRoute, MenuInspirationRoute menuInspirationRoute, DishSuggestionRoute dishSuggestionRoute)
    {
        this.allergenRoute = allergenRoute;
        this.userRoute = userRoute;
        this.stationRoute = stationRoute;
        this.menuInspirationRoute = menuInspirationRoute;
        this.dishSuggestionRoute = dishSuggestionRoute;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            get("/", ctx -> ctx.result("Welcome to miseOS!"));
            allergenRoute.getRoutes().addEndpoints();
            userRoute.getRoutes().addEndpoints();
            stationRoute.getRoutes().addEndpoints();
            menuInspirationRoute.getRoutes().addEndpoints();
            dishSuggestionRoute.getRoutes().addEndpoints();
        };
    }
}
