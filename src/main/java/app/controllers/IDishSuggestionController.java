package app.controllers;

import io.javalin.http.Context;

public interface IDishSuggestionController extends ICrudController
{
    void getCurrentWeek(Context ctx);

    void approveSuggestion(Context ctx);

    void rejectSuggestion(Context ctx);
}
