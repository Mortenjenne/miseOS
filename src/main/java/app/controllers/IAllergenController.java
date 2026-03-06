package app.controllers;

import io.javalin.http.Context;

public interface IAllergenController extends IController
{ ;
    void getByName(Context ctx);

    void seedEUAllergens(Context ctx);
}
