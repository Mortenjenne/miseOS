package app.persistence.daos.interfaces;

import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.entities.TakeAwayOrder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface ITakeAwayOrderDAO extends IEntityDAO<TakeAwayOrder, Long>
{
    Set<TakeAwayOrder> findByOfferId(Long offerId);

    Optional<Long> sumSoldQuantityByDate(LocalDate date);

    Optional<Long> countOrdersByDate(LocalDate date);

    Set<TakeAwayOrder> findByDate(LocalDate date);
}
