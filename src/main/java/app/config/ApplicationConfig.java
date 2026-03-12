package app.config;

import app.controllers.*;
import app.routes.*;
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
        Routes routes = buildRoutes(di);
        IExceptionController exceptionController = new ExceptionController();
        ServerConfig serverConfig = new ServerConfig(routes, exceptionController);

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

    private static Routes buildRoutes(DIContainer di)
    {
        return new Routes(
            new AllergenRoute(di.getAllergenController()),
            new UserRoute(di.getUserController()),
            new StationRoute(di.getStationController()),
            new MenuInspirationRoute(di.getMenuInspirationController()),
            new DishSuggestionRoute(di.getDishSuggestionController()),
            new DishRoute(di.getDishController())
        );
    }
}
