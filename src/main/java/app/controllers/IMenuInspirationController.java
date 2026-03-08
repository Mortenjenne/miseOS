package app.controllers;

import io.javalin.http.Context;

public interface IMenuInspirationController
{
    void getDailyInspiration(Context ctx);
}
