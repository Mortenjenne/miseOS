package app.config;

import app.controllers.ExceptionController;
import app.controllers.IExceptionController;
import app.routes.AllergenRoute;
import app.routes.Routes;
import app.routes.StationRoute;
import app.routes.UserRoute;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig
{
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    public static void startServer(int port)
    {
        DIContainer di = DIContainer.getInstance();
        Routes routes = buildRoutes(di);
        IExceptionController exceptionController = new ExceptionController();
        ServerConfig serverConfig = new ServerConfig(routes, exceptionController);

        Javalin app = serverConfig.create();
        app.start(port);
        logger.info("Starting javalin app");
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
            new StationRoute(di.getStationController())
        );
    }
}
