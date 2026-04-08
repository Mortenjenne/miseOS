package app.persistence.daos.interfaces;

import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.entities.TakeAwayOffer;

import java.time.LocalDate;
import java.util.Set;

public interface ITakeAwayOfferDAO extends IEntityDAO<TakeAwayOffer, Long>
{
    Set<TakeAwayOffer> findByFilter(LocalDate date, Boolean isSoldOut, Boolean isEnabled, Long dishId);

    boolean existsByDishAndDate(Long dishId, LocalDate date);

    boolean isUsedInAnyOrders(Long offerId);
}
