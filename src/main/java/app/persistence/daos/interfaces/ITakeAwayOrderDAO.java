package app.persistence.daos.interfaces;

import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.entities.TakeAwayOrder;

import java.time.LocalDate;
import java.util.Set;

public interface ITakeAwayOderDAO extends IEntityDAO<TakeAwayOrder, Long>
{
    Set<TakeAwayOrder> findByOfferId(Long offerId);

    Long sumSoldQuantityByDate(LocalDate date);

    Set<TakeAwayOrder> findByDate(LocalDate date);
}
