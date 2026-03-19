package app.controllers;

import app.exceptions.*;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;

public interface IExceptionController
{
    void handleIllegalArgument(IllegalArgumentException e, Context ctx);

    void handleValidationException(ValidationException e, Context ctx);

    void handleEntityNotFound(EntityNotFoundException e, Context ctx);

    void handleIllegalState(IllegalStateException e, Context ctx);

    void handleConflict(ConflictException e, Context ctx);

    void handleUnauthorized(UnauthorizedActionException e, Context ctx);

    void handleDatabase(DatabaseException e, Context ctx);

    void handleAIIntegration(AIIntegrationException e, Context ctx);

    void handleWeatherIntegration(WeatherIntegrationException e, Context ctx);

    void handleTranslation(TranslationException e, Context ctx);

    void handleGenericException(Exception e, Context ctx);

    void handleAuthentication(AuthenticationException e, Context ctx);
}
