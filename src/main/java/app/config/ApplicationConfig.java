package app.config;

import app.routes.Routes;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig
{
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final Routes routes = new Routes();
    private static final ServerConfig serverConfig = new ServerConfig(routes);

    public static void startServer(int port)
    {
        Javalin app = serverConfig.create();
        app.start(port);
        logger.info("Starting javalin app");
    }

    public static void stopServer(Javalin app)
    {
        app.stop();
        logger.info("Stopping javalin app");
    }
}
