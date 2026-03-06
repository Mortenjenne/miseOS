package app.controllers;

import app.dtos.exception.ErrorResponseDTO;
import app.exceptions.ValidationException;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class ExceptionController
{
    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    public void handleIllegalArgument(IllegalArgumentException e, Context ctx)
    {
        logger.warn("Bad argument [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(400, e.getMessage(), ctx);
    }

    public void handleValidationException(ValidationException e, Context ctx)
    {
        logger.error("Validation error [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
        buildErrorResponse(400, e.getMessage(), ctx);
    }

    public void handleEntityNotFound(EntityNotFoundException e, Context ctx)
    {
        buildErrorResponse(404, e.getMessage(), ctx);
    }

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
