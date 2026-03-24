package app.services;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.security.AuthenticatedUser;

import java.util.List;
import java.util.function.Consumer;

public interface IMenuInspirationService
{
    List<AiDishSuggestionDTO> getDailyInspiration(AuthenticatedUser authUser);

    void streamDailyInspiration(AuthenticatedUser authUser, Consumer<String> statusConsumer,  Consumer<AiDishSuggestionDTO> dishConsumer, Runnable onComplete, Consumer<Throwable> errorConsumer);
}
