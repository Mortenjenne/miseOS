package app.controllers;

import app.dtos.exception.ErrorResponseDTO;
import app.exceptions.*;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class ExceptionController implements IExceptionController
{
    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    @Override
    public void handleIllegalArgument(IllegalArgumentException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.warn("[{}] Bad argument [{}] {}: {}", reqId, ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(400, e.getMessage(), ctx, reqId);
    }

    @Override
    public void handleValidationException(ValidationException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.warn("[{}] Validation error [{}] {}: {}", reqId, ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(400, e.getMessage(), ctx, reqId);
    }

    @Override
    public void handleEntityNotFound(EntityNotFoundException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.warn("[{}] Entity not found [{}] {}: {}", reqId, ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(404, e.getMessage(), ctx, reqId);
    }

    @Override
    public void handleIllegalState(IllegalStateException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.error("[{}] Illegal state [{}] {}: {}", reqId, ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(409, e.getMessage(), ctx, reqId);
    }

    @Override
    public void handleUnauthorized(UnauthorizedActionException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.error("[{}] Unauthorized [{}] {}: {}", reqId, ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(403, e.getMessage(), ctx, reqId);
    }

    @Override
    public void handleDatabase(DatabaseException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.error("[{}] Database [{}] {}: {}", reqId,  ctx.method(), ctx.path(), e.getMessage(), e);
        buildErrorResponse(500, "Database error occurred. Reference: " + reqId , ctx, reqId);
    }

    @Override
    public void handleAIIntegration(AIIntegrationException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.error("[{}] AIIntegration [{}] {}: {}", reqId, ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(502, "External service unavailable", ctx, reqId);
    }

    @Override
    public void handleWeatherIntegration(WeatherIntegrationException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.error("[{}] WeatherIntegration [{}] {}: {}", reqId, ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(502, "External service unavailable", ctx, reqId);
    }

    @Override
    public void handleTranslation(TranslationException e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.error("[{}] TranslationIntegration [{}] {}: {}", reqId, ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(502, "External service unavailable", ctx, reqId);
    }

    @Override
    public void handleGenericException(Exception e, Context ctx)
    {
        String reqId = ctx.attribute("request-id");
        logger.error("[{}] Unhandled exception [{}] {}", reqId, ctx.method(), ctx.path(), e);
        buildErrorResponse(500, "An unexpected error occurred. Reference: " + reqId, ctx, reqId);
    }

    private void buildErrorResponse(int status, String message, Context ctx, String referenceId)
    {
        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
            status,
            message,
            LocalDateTime.now(),
            ctx.path(),
            referenceId
        );

        ctx.status(status);
        ctx.json(responseDTO);
    }
}
