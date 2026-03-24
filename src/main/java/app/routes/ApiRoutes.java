package app.routes;

import app.routes.resources.*;
import app.routes.security.SecurityRoute;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;

public class ApiRoutes
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

    public ApiRoutes(SecurityRoute securityRoute, AllergenRoute allergenRoute, UserRoute userRoute, StationRoute stationRoute, MenuInspirationRoute menuInspirationRoute, DishSuggestionRoute dishSuggestionRoute, DishRoute dishRoute, WeeklyMenuRoute weeklyMenuRoute, IngredientRequestRoute ingredientRequestRoute, ShoppingListRoute shoppingListRoute, NotificationRoute notificationRoute)
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
            get("/health", ctx -> ctx.status(200).result("OK"));
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
