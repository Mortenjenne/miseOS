package app.config;

import app.routes.Routes;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConfig
{
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private static final String START_TIME = "start-time";
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
            //config.routes.exception()

        });
        return app;
    }

    private void logRequest(Context ctx)
    {
        ctx.attribute(START_TIME, System.currentTimeMillis());
        String body = ctx.body();

        if (!body.isBlank())
        {
            logger.info(
                "Incoming {} {} from {} | UA: {} | Payload: {}",
                ctx.method(),
                ctx.path(),
                ctx.ip(),
                ctx.userAgent(),
                body
            );
        }
        else
        {
            logger.info(
                "Incoming {} {} from {} | UA: {}",
                ctx.method(),
                ctx.path(),
                ctx.ip(),
                ctx.userAgent()
            );
        }
    }

    private void logResponse(Context ctx)
    {
        Long start = ctx.attribute(START_TIME);
        if(start != null)
        {
            long duration = System.currentTimeMillis() - start;

            logger.info(
                "Response: {} {} -> {} ({} ms)",
                ctx.method(),
                ctx.path(),
                ctx.status(),
                duration
            );
        }
    }
}
