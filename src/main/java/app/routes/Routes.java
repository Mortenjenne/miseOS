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
    private final DishRoute dishRoute;
    private final WeeklyMenuRoute weeklyMenuRoute;

    public Routes(AllergenRoute allergenRoute, UserRoute userRoute, StationRoute stationRoute, MenuInspirationRoute menuInspirationRoute, DishSuggestionRoute dishSuggestionRoute, DishRoute dishRoute, WeeklyMenuRoute weeklyMenuRoute)
    {
        this.allergenRoute = allergenRoute;
        this.userRoute = userRoute;
        this.stationRoute = stationRoute;
        this.menuInspirationRoute = menuInspirationRoute;
        this.dishSuggestionRoute = dishSuggestionRoute;
        this.dishRoute = dishRoute;
        this.weeklyMenuRoute = weeklyMenuRoute;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            get("/", ctx -> ctx.render("index.html"));
            allergenRoute.getRoutes().addEndpoints();
            userRoute.getRoutes().addEndpoints();
            stationRoute.getRoutes().addEndpoints();
            menuInspirationRoute.getRoutes().addEndpoints();
            dishSuggestionRoute.getRoutes().addEndpoints();
            dishRoute.getRoutes().addEndpoints();
            weeklyMenuRoute.getRoutes().addEndpoints();
        };
    }
}
