package app.controllers;

import io.javalin.http.Context;

public interface ITakeAwayOrderController
{
    void getOrders(Context ctx);

    void getById(Context ctx);

    void placeOrder(Context ctx);

    void markAsPaid(Context ctx);

    void cancelOrder(Context ctx);

    void getSummary(Context ctx);
}
