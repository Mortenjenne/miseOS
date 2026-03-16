package app.routes;

import app.controllers.IStationController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public class StationRoute
{
    private final IStationController stationController;

    public StationRoute(IStationController stationController)
    {
        this.stationController = stationController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("/stations", () ->
        {
            get("/", stationController::getAll);
            get("/{id}", stationController::getById);
            get("/name/{name}", stationController::getByName);
            post("/", stationController::create);
            put("/{id}", stationController::update);
            delete("/{id}", stationController::delete);
        });
    }
}
