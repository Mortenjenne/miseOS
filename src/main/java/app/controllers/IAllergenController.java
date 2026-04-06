package app.controllers;

import io.javalin.http.Context;

public interface IAllergenController extends ICrudController
{
    void seedEUAllergens(Context ctx);

    void searchByName(Context ctx);
}
