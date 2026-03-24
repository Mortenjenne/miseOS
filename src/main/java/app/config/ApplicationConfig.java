package app.config;

import app.routes.*;
import app.routes.resources.*;
import app.routes.security.SecurityRoute;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig
{
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    public static void startServer(int port)
    {
        DIContainer di = DIContainer.getInstance();
        buildAndStart(port, di);
    }

    public static Javalin startServer(int port, EntityManagerFactory emf)
    {
        DIContainer di = DIContainer.getTestInstance(emf);
        return buildAndStart(port, di);
    }

    public static Javalin buildAndStart(int port, DIContainer di)
    {
        ApiRoutes apiRoutes = buildRoutes(di);
        ServerConfig serverConfig = new ServerConfig(apiRoutes, di.getExceptionController(), di.getSecurityController());
        Javalin app = serverConfig.create();
        logger.info("Starting javalin app");
        app.start(port);
        return app;
    }

    public static void stopServer(Javalin app)
    {
        app.stop();
        logger.info("Stopping javalin app");
    }

    private static ApiRoutes buildRoutes(DIContainer di)
    {
        return new ApiRoutes(
            new SecurityRoute(di.getSecurityController(), di.getUserController()),
            new AllergenRoute(di.getAllergenController()),
            new UserRoute(di.getUserController()),
            new StationRoute(di.getStationController()),
            new MenuInspirationRoute(di.getMenuInspirationController()),
            new DishSuggestionRoute(di.getDishSuggestionController()),
            new DishRoute(di.getDishController()),
            new WeeklyMenuRoute(di.getWeeklyMenuController()),
            new IngredientRequestRoute(di.getIngredientRequestController()),
            new ShoppingListRoute(di.getShoppingListController()),
            new NotificationRoute(di.getNotificationController())
        );
    }
}
