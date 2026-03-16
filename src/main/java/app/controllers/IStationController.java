package app.controllers;

import io.javalin.http.Context;

public interface IStationController
{
    void create(Context ctx);

    void update(Context ctx);

    void delete(Context ctx);

    void getById(Context ctx);

    void getAll(Context ctx);

    void getByName(Context ctx);
}
