package app.persistence.daos.interfaces;

import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.entities.TakeAwayOffer;

import java.time.LocalDate;
import java.util.Set;

public interface ITakeAwayOffer extends IEntityDAO<TakeAwayOffer, Long>
{
    Set<TakeAwayOffer> findActiveOffers(LocalDate date);

    boolean existsByDishAndDate(Long dishId, LocalDate date);

    Set<TakeAwayOffer> findByDate(LocalDate date);
}
