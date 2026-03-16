package app.controllers;

import io.javalin.http.Context;

public interface IDishController extends ICrudController
{
    void search(Context ctx);

    void getAvailableForMenu(Context ctx);

    void getAllGrouped(Context ctx);

    void activate(Context ctx);

    void deactivate(Context ctx);
}
