package app.services;

import app.dtos.shopping.*;
import app.enums.ShoppingListStatus;

import java.time.LocalDate;
import java.util.List;

public interface IShoppingListService
{
    ShoppingListDTO generateShoppingList(Long userId, CreateShoppingListDTO dto);

    ShoppingListDTO finalizeShoppingList(Long userId, Long shoppingListId);

    boolean deleteShoppingList(Long userId, Long shoppingListId);

    List<ShoppingListDTO> getShoppingLists(Long userId, ShoppingListStatus status, LocalDate deliveryDate);

    ShoppingListDTO markItemOrdered(Long userId, Long shoppingListId, Long itemId);

    ShoppingListDTO markAllItemsOrdered(Long shoppingListId, Long userId);

    ShoppingListDTO addItemToShoppingList(Long userId, Long shoppingListId, CreateShoppingListItemDTO dto);

    ShoppingListDTO removeItem(Long userId, Long shoppingListId, Long itemId);

    ShoppingListDTO updateItem(Long userId, Long shoppingListId, Long itemId, UpdateShoppingListItemDTO dto);

    ShoppingListDTO getById(Long shoppingListId);

    ShoppingListDTO updateDeliveryDate(Long userId, Long shoppingListId, UpdateShoppingListDTO dto);
}
