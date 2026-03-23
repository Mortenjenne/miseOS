package app.controllers.impl;

import app.controllers.IMenuInspirationController;
import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.security.AuthenticatedUser;
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
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);

        List<AiDishSuggestionDTO> suggestions = menuInspirationService.getDailyInspiration(authUser);
        ctx.status(200).json(suggestions);
    }

    @Override
    public void getStreamingSuggestions(SseClient client)
    {
        client.keepAlive();
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(client.ctx());

        menuInspirationService.streamDailyInspiration(
            authUser,
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
