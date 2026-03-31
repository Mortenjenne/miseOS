package app.routes.resources;

import app.controllers.IShoppingListController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ShoppingListRoute
{
    private final IShoppingListController shoppingListController;

    public ShoppingListRoute(IShoppingListController shoppingListController)
    {
        this.shoppingListController = shoppingListController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> path("shopping-lists", () ->
        {
            get("", shoppingListController::getShoppingLists, Role.HEAD_CHEF, Role.SOUS_CHEF);
            get("{id}", shoppingListController::getById, Role.HEAD_CHEF, Role.SOUS_CHEF);
            post("", shoppingListController::create, Role.HEAD_CHEF, Role.SOUS_CHEF);
            post("{id}/finalize", shoppingListController::finalizeShoppingList, Role.HEAD_CHEF, Role.SOUS_CHEF);
            post("{id}/items", shoppingListController::addShoppingListItem, Role.HEAD_CHEF, Role.SOUS_CHEF);
            patch("{id}/delivery-date", shoppingListController::updateDeliveryDate, Role.HEAD_CHEF, Role.SOUS_CHEF);
            delete("{id}", shoppingListController::delete, Role.HEAD_CHEF, Role.SOUS_CHEF);
            put("{id}/items/{itemId}", shoppingListController::updateShoppingListItem, Role.HEAD_CHEF, Role.SOUS_CHEF);
            delete("{id}/items/{itemId}", shoppingListController::removeShoppingListItem, Role.HEAD_CHEF, Role.SOUS_CHEF);
            patch("{id}/items/{itemId}/ordered", shoppingListController::markShoppingListItemOrdered, Role.HEAD_CHEF, Role.SOUS_CHEF);
            patch("{id}/items/ordered", shoppingListController::markAllShoppinglistItemOrdered, Role.HEAD_CHEF, Role.SOUS_CHEF);
        });
    }
}
