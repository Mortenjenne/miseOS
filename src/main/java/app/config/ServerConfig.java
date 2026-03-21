package app.config;

import app.controllers.IExceptionController;
import app.controllers.ISecurityController;
import app.exceptions.*;
import app.routes.ApiRoutes;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ServerConfig
{
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private static final String START_TIME = "start-time";
    private static final String REQ_ID = "request-id";

    private final ApiRoutes apiRoutes;
    private final IExceptionController exceptionController;
    private final ISecurityController securityController;

    public ServerConfig(ApiRoutes apiRoutes, IExceptionController exceptionController, ISecurityController securityController)
    {
        this.apiRoutes = apiRoutes;
        this.exceptionController = exceptionController;
        this.securityController = securityController;
    }

    public Javalin create()
    {
        return Javalin.create(config ->
        {
            config.startup.showJavalinBanner = false;
            config.router.contextPath = "/api/v1";
            config.bundledPlugins.enableRouteOverview("/routes");
            config.routes.apiBuilder(apiRoutes.getRoutes());
            configureMiddleWareLogging(config);
            configureMiddleWareSecurity(config);
            configureExceptions(config);
        });
    }

    private void configureMiddleWareSecurity(JavalinConfig config)
    {
        config.routes.beforeMatched(securityController::authenticate);
        config.routes.beforeMatched(securityController::authorize);
        config.routes.wsBefore("/notifications", ws -> ws.onConnect(securityController::authenticateWebSocket));
    }

    private void configureMiddleWareLogging(JavalinConfig config)
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
        config.routes.exception(ConflictException.class, exceptionController::handleConflict);
        config.routes.exception(UnauthorizedActionException.class, exceptionController::handleUnauthorized);
        config.routes.exception(AuthenticationException.class, exceptionController::handleAuthentication);
        config.routes.exception(AIIntegrationException.class, exceptionController::handleAIIntegration);
        config.routes.exception(WeatherIntegrationException.class, exceptionController::handleWeatherIntegration);
        config.routes.exception(TranslationException.class, exceptionController::handleTranslation);
        config.routes.exception(DatabaseException.class, exceptionController::handleDatabase);
        config.routes.exception(Exception.class, exceptionController::handleGenericException);
    }

    private void logRequest(Context ctx)
    {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        ctx.attribute(REQ_ID, requestId);
        ctx.attribute(START_TIME, System.currentTimeMillis());

        String body = isSensitivePath(ctx.path()) ? "[CENSORED]" : ctx.body();

        if (!body.isBlank())
        {
            logger.info(
                "[{}] Incoming {} {} from {} | UA: {} | Payload: {}",
                requestId,
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
                "[{}] Incoming {} {} from {} | UA: {}",
                requestId,
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
        String requestId = ctx.attribute(REQ_ID);

        if(start != null && requestId != null)
        {
            long duration = System.currentTimeMillis() - start;

            logger.info(
                "[{}] Response: {} {} -> {} ({} ms)",
                requestId,
                ctx.method(),
                ctx.path(),
                ctx.status(),
                duration
            );
        }
    }

    private boolean isSensitivePath(String path)
    {
        return path.contains("/login") || path.contains("/register") || path.contains("/password") || path.contains("/auth");
    }
}
