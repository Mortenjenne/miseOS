package app.services;

import app.dtos.security.AuthenticatedUser;
import app.dtos.shopping.*;
import app.enums.ShoppingListStatus;

import java.time.LocalDate;
import java.util.List;

public interface IShoppingListService
{
    ShoppingListDTO generateShoppingList(AuthenticatedUser authUser, CreateShoppingListDTO dto);

    ShoppingListDTO finalizeShoppingList(Long shoppingListId);

    boolean deleteShoppingList(AuthenticatedUser authUser, Long shoppingListId);

    List<ShoppingListDTO> getShoppingLists(ShoppingListStatus status, LocalDate deliveryDate);

    ShoppingListDTO markItemOrdered(Long shoppingListId, Long itemId);

    ShoppingListDTO markAllItemsOrdered(Long shoppingListId);

    ShoppingListDTO addItemToShoppingList(AuthenticatedUser authUser, Long shoppingListId, CreateShoppingListItemDTO dto);

    ShoppingListDTO removeItem(Long shoppingListId, Long itemId);

    ShoppingListDTO updateItem(Long shoppingListId, Long itemId, UpdateShoppingListItemDTO dto);

    ShoppingListDTO getById(Long shoppingListId);

    ShoppingListDTO updateDeliveryDate(Long shoppingListId, UpdateShoppingListDTO dto);
}
