package app.controllers;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.services.IMenuInspirationService;
import app.utils.SecurityUtil;
import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;

import java.util.List;

public class MenuInspirationController implements IMenuInspirationController
{
    private final IMenuInspirationService menuInspirationService;

    public MenuInspirationController(IMenuInspirationService menuInspirationService)
    {
        this.menuInspirationService = menuInspirationService;
    }

    @Override
    public void getDailyInspiration(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);

        List<AiDishSuggestionDTO> suggestions = menuInspirationService.getDailyInspiration(userId);
        ctx.status(200).json(suggestions);
    }

    @Override
    public void getStreamingSuggestions(SseClient client)
    {
        client.keepAlive();
        Long userId = SecurityUtil.requireUserId(client.ctx());

        menuInspirationService.streamDailyInspiration(
            userId,
            status -> client.sendEvent("status", status),
            dish -> client.sendEvent("dish", dish),
            () ->
            {
                client.sendEvent("done", "complete");
                client.close();
            },

            error ->
            {
                client.sendEvent("error", error.getMessage());
                client.close();
            }
        );
    }
}
