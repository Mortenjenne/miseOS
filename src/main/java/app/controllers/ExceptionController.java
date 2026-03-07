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
        logger.error("Bad argument [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(400, e.getMessage(), ctx);
    }

    @Override
    public void handleValidationException(ValidationException e, Context ctx)
    {
        logger.error("Validation error [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(400, e.getMessage(), ctx);
    }

    @Override
    public void handleEntityNotFound(EntityNotFoundException e, Context ctx)
    {
        logger.error("Entity not found [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(404, e.getMessage(), ctx);
    }

    @Override
    public void handleIllegalState(IllegalStateException e, Context ctx)
    {
        logger.error("Illegal state [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(409, e.getMessage(), ctx);
    }

    @Override
    public void handleUnauthorized(UnauthorizedActionException e, Context ctx)
    {
        logger.error("Unauthorized [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(403, e.getMessage(), ctx);
    }

    @Override
    public void handleDatabase(DatabaseException e, Context ctx)
    {
        logger.error("Database [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage(), e);
        buildErrorResponse(500, "Database error occurred", ctx);
    }

    @Override
    public void handleAIIntegration(AIIntegrationException e, Context ctx)
    {
        logger.error("AIIntergration [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(502, e.getMessage(), ctx);
    }

    @Override
    public void handleWeatherIntegration(WeatherIntegrationException e, Context ctx)
    {
        logger.error("WeatherIntegration [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(502, e.getMessage(), ctx);
    }

    @Override
    public void handleTranslation(TranslationException e, Context ctx)
    {
        logger.error("TranslationIntegration [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(502, e.getMessage(), ctx);
    }

    @Override
    public void handleGenericException(Exception e, Context ctx)
    {
        logger.error("Unhandled exception [{}] {}", ctx.method(), ctx.path(), e);
        buildErrorResponse(500, "An unexpected error occurred", ctx);
    }

    private void buildErrorResponse(int status, String message, Context ctx)
    {
        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
            status,
            message,
            LocalDateTime.now(),
            ctx.path()
        );

        ctx.status(status);
        ctx.json(responseDTO);
    }
}
