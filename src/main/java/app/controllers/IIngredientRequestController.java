package app.controllers;

import io.javalin.http.Context;

public interface IIngredientRequestController extends ICrudController
{
    void approve(Context ctx);

    void reject(Context ctx);
}
