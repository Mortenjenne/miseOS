package app.controllers;

import app.services.IAllergenService;
import io.javalin.http.Context;

public class AllergenController implements IAllergenController
{
    private final IAllergenService allergenService;

    public AllergenController(IAllergenService allergenService)
    {
        this.allergenService = allergenService;
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

    @Override
    public void getByName(Context ctx)
    {

    }

    @Override
    public void seedEUAllergens(Context ctx)
    {

    }
}
