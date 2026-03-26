package app.mappers;

import app.dtos.shopping.ShoppingListDTO;
import app.dtos.shopping.ShoppingListItemDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.ShoppingList;
import app.persistence.entities.ShoppingListItem;

import java.util.Comparator;
import java.util.List;

public class ShoppingListMapper
{
    private ShoppingListMapper(){}

    public static ShoppingListDTO toDTO(ShoppingList shoppingList)
    {
        List<ShoppingListItemDTO> shoppingListItemDTOS = shoppingList.getShoppingListItems()
            .stream()
            .sorted(Comparator.comparing(ShoppingListItem::getIngredientName))
            .map(ShoppingListMapper::toItemDTO)
            .toList();

        UserReferenceDTO createdBy = UserMapper.toReferenceDTO(shoppingList.getCreatedBy());

        return new ShoppingListDTO(
            shoppingList.getId(),
            shoppingList.getDeliveryDate(),
            shoppingList.getShoppingListStatus(),
            createdBy,
            shoppingList.getItemCount(),
            shoppingListItemDTOS,
            shoppingList.allItemsOrdered(),
            shoppingList.isNormalized(),
            shoppingList.getCreatedAt(),
            shoppingList.getFinalizedAt()
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
            shoppingListItem.isOrdered(),
            shoppingListItem.getCreatedAt(),
            shoppingListItem.getUpdatedAt()
        );
    }
}
