package app.routes.ressources;

import app.controllers.IStationController;
import app.enums.Role;
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
            get("/", stationController::getAll, Role.HEAD_CHEF, Role.SOUS_CHEF);
            get("/{id}", stationController::getById, Role.HEAD_CHEF, Role.SOUS_CHEF);
            get("/name/{name}", stationController::getByName, Role.HEAD_CHEF, Role.SOUS_CHEF);
            post("/", stationController::create, Role.HEAD_CHEF);
            put("/{id}", stationController::update, Role.HEAD_CHEF);
            delete("/{id}", stationController::delete, Role.HEAD_CHEF);
        });
    }
}
