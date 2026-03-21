package app.routes.ressources;

import app.controllers.IDishSuggestionController;
import app.enums.Role;
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
            get("/", dishSuggestionController::getAll, Role.KITCHEN_STAFF);
            get("/current-week",dishSuggestionController::getCurrentWeek, Role.HEAD_CHEF, Role.SOUS_CHEF);
            get("/{id}", dishSuggestionController::getById, Role.KITCHEN_STAFF);
            post("/", dishSuggestionController::create, Role.KITCHEN_STAFF);
            put("/{id}", dishSuggestionController::update, Role.KITCHEN_STAFF);
            delete("/{id}",dishSuggestionController::delete, Role.KITCHEN_STAFF);
            delete("/{id}/allergens/{allergenId}", dishSuggestionController::removeAllergen, Role.KITCHEN_STAFF);
            patch("/{id}/approve", dishSuggestionController::approveSuggestion, Role.HEAD_CHEF, Role.SOUS_CHEF);
            patch("/{id}/reject", dishSuggestionController::rejectSuggestion, Role.HEAD_CHEF, Role.SOUS_CHEF);
        });
    }
}
