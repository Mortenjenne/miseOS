package app.controllers;

import io.javalin.http.Context;

public interface IWeeklyMenuController
{
    void getById(Context ctx);

    void getAll(Context ctx);

    void create(Context ctx);

    void delete(Context ctx);

    void addMenuSlot(Context ctx);

    void removeMenuSlot(Context ctx);

    void updateMenuSlot(Context ctx);

    void translateMenu(Context ctx);

    void publishMenu(Context ctx);

    void getCurrentWeekMenu(Context ctx);

    void getByWeekAndYear(Context ctx);

    void translateSlot(Context ctx);
}
