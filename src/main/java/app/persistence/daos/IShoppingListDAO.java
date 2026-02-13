package app.persistence.daos;

import app.enums.ShoppingListStatus;
import app.persistence.entities.ShoppingList;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface IShoppingListDAO
{
    Set<ShoppingList> findByStatus(ShoppingListStatus status);
    Optional<ShoppingList> findByDeliveryDate(LocalDate deliveryDate);
    Optional<ShoppingList> getByIdWithItems(Long id);
}
