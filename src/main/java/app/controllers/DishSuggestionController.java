package app.controllers;

import app.services.IDishSuggestionService;
import io.javalin.http.Context;

public class DishSuggestionController implements IDishSuggestionController
{
    private final IDishSuggestionService dishSuggestionService;

    public DishSuggestionController(IDishSuggestionService dishSuggestionService)
    {
        this.dishSuggestionService = dishSuggestionService;
    }

    @Override
    public void approveSuggestion(Context ctx)
    {

    }

    @Override
    public void rejectSuggestion(Context ctx)
    {

    }

    @Override
    public void getByIdWithAllergens(Context ctx)
    {

    }

    @Override
    public void getAllPending(Context ctx)
    {

    }

    @Override
    public void getPendingForWeek(Context ctx)
    {

    }

    @Override
    public void getApprovedForWeek(Context ctx)
    {

    }

    @Override
    public void getByStatus(Context ctx)
    {

    }

    @Override
    public void getById(Context ctx)
    {

    }

    @Override
    public void getAll(Context ctx)
    {

    }

    @Override
    public void create(Context ctx)
    {

    }

    @Override
    public void update(Context ctx)
    {

    }

    @Override
    public void delete(Context ctx)
    {

    }
}
