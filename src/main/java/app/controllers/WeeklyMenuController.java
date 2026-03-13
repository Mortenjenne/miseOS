package app.controllers;

import app.services.IWeeklyMenuService;
import io.javalin.http.Context;

public class WeeklyMenuController implements IWeeklyMenuController
{
    private final IWeeklyMenuService weeklyMenuService;

    public WeeklyMenuController(IWeeklyMenuService weeklyMenuService)
    {
        this.weeklyMenuService = weeklyMenuService;
    }

    @Override
    public void addMenuSlot(Context ctx)
    {

    }

    @Override
    public void removeMenuSlot(Context ctx)
    {

    }

    @Override
    public void updateMenuSlot(Context ctx)
    {

    }

    @Override
    public void translateMenu(Context ctx)
    {

    }

    @Override
    public void publishMenu(Context ctx)
    {

    }

    @Override
    public void getCurrentWeekMenu(Context ctx)
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
