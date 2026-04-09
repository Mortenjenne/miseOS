package app.persistence.daos.interfaces;

import app.dtos.takeaway.TakeAwayOrderCreateDTO;
import app.enums.OrderStatus;
import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.entities.TakeAwayOrder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface ITakeAwayOrderDAO
{
    Set<TakeAwayOrder> findByFilter(Long customerId, Long offerId, LocalDate date, OrderStatus status);

    Optional<Long> sumSoldQuantityByDate(LocalDate date);

    Optional<Long> countOrdersByDate(LocalDate date);

    TakeAwayOrder getByID(Long id);

    TakeAwayOrder create(Long customerId, TakeAwayOrderCreateDTO dto);

    TakeAwayOrder update(TakeAwayOrder order);

    boolean delete(Long id);
}
