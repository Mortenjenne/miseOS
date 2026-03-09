package app.controllers;

import io.javalin.http.Context;

public interface IAllergenController extends ICrudController
{ ;
    void getByName(Context ctx);

    void seedEUAllergens(Context ctx);
}
