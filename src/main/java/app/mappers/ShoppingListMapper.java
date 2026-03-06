package app.mappers;

import app.dtos.shopping.ShoppingListDTO;
import app.dtos.shopping.ShoppingListItemDTO;
import app.persistence.entities.ShoppingList;
import app.persistence.entities.ShoppingListItem;

import java.util.List;

public class ShoppingListMapper
{
    private ShoppingListMapper(){}

    public static ShoppingListDTO toDTO(ShoppingList shoppingList)
    {
        List<ShoppingListItemDTO> shoppingListItemDTOS = shoppingList.getShoppingListItems().stream()
            .map(ShoppingListMapper::toItemDTO)
            .toList();

        return new ShoppingListDTO(
            shoppingList.getId(),
            shoppingList.getDeliveryDate(),
            shoppingList.getShoppingListStatus().name(),
            shoppingList.getCreatedBy().getFirstName(),
            shoppingList.getItemCount(),
            shoppingListItemDTOS,
            shoppingList.allItemsOrdered()
        );
    }

    public static ShoppingListItemDTO toItemDTO(ShoppingListItem shoppingListItem)
    {
        return new ShoppingListItemDTO(
            shoppingListItem.getId(),
            shoppingListItem.getIngredientName(),
            shoppingListItem.getQuantity(),
            shoppingListItem.getUnit(),
            shoppingListItem.getSupplier(),
            shoppingListItem.getNotes(),
            shoppingListItem.isOrdered()
        );
    }
}
