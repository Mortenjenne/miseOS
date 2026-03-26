package app.controllers;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public interface IAllergenController extends ICrudController
{
    void seedEUAllergens(Context ctx);

    void searchByName(@NotNull Context context);
}
