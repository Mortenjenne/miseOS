package app.controllers;

import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;
import org.jetbrains.annotations.NotNull;

public interface IMenuInspirationController
{
    void getDailyInspiration(Context ctx);

    void getStreamingSuggestions(SseClient client);
}
