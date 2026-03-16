package app.services;

import app.dtos.gemini.AiDishSuggestionDTO;

import java.util.List;
import java.util.function.Consumer;

public interface IMenuInspirationService
{
    List<AiDishSuggestionDTO> getDailyInspiration(Long chefId);

    void streamDailyInspiration(Long chefId, Consumer<String> statusConsumer,  Consumer<AiDishSuggestionDTO> dishConsumer, Runnable onComplete, Consumer<Throwable> errorConsumer);
}
