package app.routes;

import app.controllers.IDishSuggestionController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;


public class DishSuggestionRoute
{
    private final IDishSuggestionController dishSuggestionController;

    public DishSuggestionRoute(IDishSuggestionController dishSuggestionController)
    {
        this.dishSuggestionController = dishSuggestionController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("dish-suggestions", () ->
        {
            get("/", dishSuggestionController::getAll);
            get("/current-week",dishSuggestionController::getCurrentWeek);
            get("/{id}", dishSuggestionController::getById);
            post("/", dishSuggestionController::create);
            put("/{id}", dishSuggestionController::update);
            delete("/{id}",dishSuggestionController::delete);
            patch("/{id}/approve", dishSuggestionController::approveSuggestion);
            patch("/{id}/reject", dishSuggestionController::rejectSuggestion);
        });
    }
}
