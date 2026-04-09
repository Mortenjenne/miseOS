package app.controllers;

import io.javalin.http.Context;

public interface ITakeAwayOfferController extends ICrudController
{
    void enableOffer(Context ctx);

    void disableOffer(Context ctx);
}
