package app.config;

import app.routes.Routes;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConfig
{
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private final Routes routes;

    public ServerConfig(Routes routes)
    {
        this.routes = routes;
    }

    public Javalin create()
    {
        Javalin app = Javalin.create(config ->
        {
            config.startup.showJavalinBanner = false;
            config.router.contextPath = "/api/v1";
            config.bundledPlugins.enableRouteOverview("/routes");
            config.routes.apiBuilder(routes.getRoutes());
            config.routes.before(this::logRequest);
            config.routes.after(this::logResponse);

        });
        return app;
    }

    private void logRequest(Context ctx) {
        logger.info("Incoming request from {}: {} {}", ctx.ip(), ctx.method(), ctx.path());
    }

    private void logResponse(Context ctx) {
        logger.info("Response: {} {} -> {}", ctx.method(), ctx.path(), ctx.status());
    }
}
