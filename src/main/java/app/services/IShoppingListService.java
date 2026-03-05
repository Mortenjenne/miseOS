package app.services;

import app.dtos.shopping.CreateShoppingListDTO;
import app.dtos.shopping.CreateShoppingListItemDTO;
import app.dtos.shopping.ShoppingListDTO;
import app.dtos.shopping.UpdateShoppingListItemDTO;
import app.enums.ShoppingListStatus;

import java.time.LocalDate;
import java.util.Set;

public interface IShoppingListService
{
    ShoppingListDTO generateShoppingList(Long userId, CreateShoppingListDTO dto);

    ShoppingListDTO finalizeShoppingList(Long userId, Long shoppingListId);

    boolean deleteShoppingList(Long userId, Long shoppingListId);

    Set<ShoppingListDTO> getAll();

    Set<ShoppingListDTO> findByStatus(ShoppingListStatus status);

    ShoppingListDTO markItemOrdered(Long userId, Long shoppingListId, Long itemId);

    ShoppingListDTO markAllItemsOrdered(Long shoppingListId, Long userId);

    ShoppingListDTO addItemToShoppingList(Long userId, Long shoppingListId, CreateShoppingListItemDTO dto);

    ShoppingListDTO removeItem(Long userId, Long shoppingListId, Long itemId);

    ShoppingListDTO updateItem(Long userId, Long shoppingListId, Long itemId, UpdateShoppingListItemDTO dto);

    ShoppingListDTO getById(Long shoppingListId);

    ShoppingListDTO findByDeliveryDate(LocalDate deliveryDate);
}
