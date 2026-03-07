package app.config;

import app.controllers.IExceptionController;
import app.exceptions.*;
import app.routes.Routes;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConfig
{
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private static final String START_TIME = "start-time";
    private final Routes routes;
    private final IExceptionController exceptionController;

    public ServerConfig(Routes routes, IExceptionController exceptionController)
    {
        this.routes = routes;
        this.exceptionController = exceptionController;
    }

    public Javalin create()
    {
        Javalin app = Javalin.create(config ->
        {
            config.startup.showJavalinBanner = false;
            config.router.contextPath = "/api/v1";
            config.bundledPlugins.enableRouteOverview("/routes");
            config.routes.apiBuilder(routes.getRoutes());
            configureMiddleWare(config);
            configureExceptions(config);
        });
        return app;
    }

    private void configureMiddleWare(JavalinConfig config)
    {
        config.routes.before(this::logRequest);
        config.routes.after(this::logResponse);
    }

    private void configureExceptions(JavalinConfig config)
    {
        config.routes.exception(IllegalArgumentException.class, exceptionController::handleIllegalArgument);
        config.routes.exception(ValidationException.class, exceptionController::handleValidationException);
        config.routes.exception(EntityNotFoundException.class, exceptionController::handleEntityNotFound);
        config.routes.exception(IllegalStateException.class, exceptionController::handleIllegalState);
        config.routes.exception(UnauthorizedActionException.class, exceptionController::handleUnauthorized);
        config.routes.exception(AIIntegrationException.class, exceptionController::handleAIIntegration);
        config.routes.exception(WeatherIntegrationException.class, exceptionController::handleWeatherIntegration);
        config.routes.exception(TranslationException.class, exceptionController::handleTranslation);
        config.routes.exception(DatabaseException.class, exceptionController::handleDatabase);
        config.routes.exception(Exception.class, exceptionController::handleGenericException);
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
