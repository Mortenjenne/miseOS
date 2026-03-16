package app.persistence.daos.interfaces;

import app.enums.ShoppingListStatus;
import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.entities.ShoppingList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IShoppingListDAO extends IEntityDAO<ShoppingList, Long>
{
    Optional<ShoppingList> findByDeliveryDate(LocalDate deliveryDate);

    List<ShoppingList> findByFilter(ShoppingListStatus status, LocalDate deliveryDate);
}
