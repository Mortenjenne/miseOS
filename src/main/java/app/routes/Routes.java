package app.routes;

import io.javalin.apibuilder.EndpointGroup;

public class Routes
{
    private final SecurityRoute securityRoute;
    private final AllergenRoute allergenRoute;
    private final UserRoute userRoute;
    private final StationRoute stationRoute;
    private final MenuInspirationRoute menuInspirationRoute;
    private final DishSuggestionRoute dishSuggestionRoute;
    private final DishRoute dishRoute;
    private final WeeklyMenuRoute weeklyMenuRoute;
    private final IngredientRequestRoute ingredientRequestRoute;
    private final ShoppingListRoute shoppingListRoute;
    private final NotificationRoute notificationRoute;

    public Routes(SecurityRoute securityRoute, AllergenRoute allergenRoute, UserRoute userRoute, StationRoute stationRoute, MenuInspirationRoute menuInspirationRoute, DishSuggestionRoute dishSuggestionRoute, DishRoute dishRoute, WeeklyMenuRoute weeklyMenuRoute, IngredientRequestRoute ingredientRequestRoute, ShoppingListRoute shoppingListRoute, NotificationRoute notificationRoute)
    {
        this.securityRoute = securityRoute;
        this.allergenRoute = allergenRoute;
        this.userRoute = userRoute;
        this.stationRoute = stationRoute;
        this.menuInspirationRoute = menuInspirationRoute;
        this.dishSuggestionRoute = dishSuggestionRoute;
        this.dishRoute = dishRoute;
        this.weeklyMenuRoute = weeklyMenuRoute;
        this.ingredientRequestRoute = ingredientRequestRoute;
        this.shoppingListRoute = shoppingListRoute;
        this.notificationRoute = notificationRoute;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            securityRoute.getRoutes().addEndpoints();
            allergenRoute.getRoutes().addEndpoints();
            userRoute.getRoutes().addEndpoints();
            stationRoute.getRoutes().addEndpoints();
            menuInspirationRoute.getRoutes().addEndpoints();
            dishSuggestionRoute.getRoutes().addEndpoints();
            dishRoute.getRoutes().addEndpoints();
            weeklyMenuRoute.getRoutes().addEndpoints();
            ingredientRequestRoute.getRoutes().addEndpoints();
            shoppingListRoute.getRoutes().addEndpoints();
            notificationRoute.getRoutes().addEndpoints();
        };
    }
}
