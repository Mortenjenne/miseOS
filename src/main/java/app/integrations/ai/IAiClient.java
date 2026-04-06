package app.integrations.ai;

import java.util.function.Consumer;

public interface IAiClient
{
    String generateResponse(String prompt);

    void streamResponse(String prompt, Consumer<String> chunkConsumer, Consumer<Throwable> errorConsumer, Runnable onComplete);

}
