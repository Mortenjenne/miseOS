package app.controllers;

import io.javalin.http.Context;

public interface IShoppingListController
{
    void create(Context ctx);

    void getById(Context ctx);

    void delete(Context ctx);

    void updateDeliveryDate(Context ctx);

    void getShoppingLists(Context ctx);

    void addShoppingListItem(Context ctx);

    void removeShoppingListItem(Context ctx);

    void updateShoppingListItem(Context ctx);

    void markShoppingListItemOrdered(Context ctx);

    void markAllShoppinglistItemOrdered(Context ctx);

    void finalizeShoppingList(Context ctx);


}
