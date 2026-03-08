package app.controllers;

import app.services.IStationService;
import io.javalin.http.Context;

public class StationController implements IStationController
{
    private final IStationService stationService;

    public StationController(IStationService stationService)
    {
        this.stationService = stationService;
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
    public void getById(Context ctx)
    {

    }

    @Override
    public void getAll(Context ctx)
    {

    }

    @Override
    public void getByName(Context ctx)
    {

    }
}
