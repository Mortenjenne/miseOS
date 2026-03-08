package app.controllers;

import io.javalin.http.Context;

public interface IDishSuggestionController extends IController
{
    void approveSuggestion(Context ctx);

    void rejectSuggestion(Context ctx);

    void getByIdWithAllergens(Context ctx);

    void getAllPending(Context ctx);

    void getPendingForWeek(Context ctx);

    void getApprovedForWeek(Context ctx);

    void getByStatus(Context ctx);
}
