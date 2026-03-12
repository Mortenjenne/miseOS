package app.integrations.ai;

import app.dtos.gemini.AiDishSuggestionDTO;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface IAiClient
{
    String generateResponse(String prompt);

    void streamResponse(String prompt, Consumer<String> chunkConsumer, Consumer<Throwable> errorConsumer, Runnable onComplete);

}
