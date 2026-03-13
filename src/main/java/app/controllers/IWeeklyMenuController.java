package app.controllers;

import io.javalin.http.Context;

public interface IWeeklyMenuController extends ICrudController
{
    void addMenuSlot(Context ctx);

    void removeMenuSlot(Context ctx);

    void updateMenuSlot(Context ctx);

    void translateMenu(Context ctx);

    void publishMenu(Context ctx);

    void getCurrentWeekMenu(Context ctx);
}
