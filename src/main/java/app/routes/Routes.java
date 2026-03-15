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
    private final IngredientRequestRoute ingredientRequestRoute;

    public Routes(AllergenRoute allergenRoute, UserRoute userRoute, StationRoute stationRoute, MenuInspirationRoute menuInspirationRoute, DishSuggestionRoute dishSuggestionRoute, DishRoute dishRoute, WeeklyMenuRoute weeklyMenuRoute, IngredientRequestRoute ingredientRequestRoute)
    {
        this.allergenRoute = allergenRoute;
        this.userRoute = userRoute;
        this.stationRoute = stationRoute;
        this.menuInspirationRoute = menuInspirationRoute;
        this.dishSuggestionRoute = dishSuggestionRoute;
        this.dishRoute = dishRoute;
        this.weeklyMenuRoute = weeklyMenuRoute;
        this.ingredientRequestRoute = ingredientRequestRoute;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            allergenRoute.getRoutes().addEndpoints();
            userRoute.getRoutes().addEndpoints();
            stationRoute.getRoutes().addEndpoints();
            menuInspirationRoute.getRoutes().addEndpoints();
            dishSuggestionRoute.getRoutes().addEndpoints();
            dishRoute.getRoutes().addEndpoints();
            weeklyMenuRoute.getRoutes().addEndpoints();
            ingredientRequestRoute.getRoutes().addEndpoints();
        };
    }
}
