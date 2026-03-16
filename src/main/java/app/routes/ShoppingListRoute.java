package app.routes;

import app.controllers.IShoppingListController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ShoppingListRoute
{
    private final IShoppingListController shoppingListController;

    public ShoppingListRoute(IShoppingListController shoppingListController)
    {
        this.shoppingListController = shoppingListController;
    }

    protected EndpointGroup getRoutes()
    {
        return () -> path("/shopping-lists", () ->
        {
            get("/", shoppingListController::getShoppingLists);
            post("/", shoppingListController::create);
            get("/{id}", shoppingListController::getById);
            patch("/{id}/delivery-date", shoppingListController::updateDeliveryDate);
            delete("/{id}", shoppingListController::delete);
            post("/{id}/finalize", shoppingListController::finalizeShoppingList);
            post("/{id}/items", shoppingListController::addShoppingListItem);
            delete("/{id}/items/{itemId}", shoppingListController::removeShoppingListItem);
            put("/{id}/items/{itemId}", shoppingListController::updateShoppingListItem);
            patch("/{id}/items/{itemId}/ordered", shoppingListController::markShoppingListItemOrdered);
            patch("/{id}/items/ordered", shoppingListController::markAllShoppinglistItemOrdered);
        });
    }

}
