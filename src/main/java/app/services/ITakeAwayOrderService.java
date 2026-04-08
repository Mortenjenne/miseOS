package app.services;

import app.dtos.security.AuthenticatedUser;
import app.dtos.takeaway.TakeAwayOrderCreateDTO;
import app.dtos.takeaway.TakeAwayOrderDTO;
import app.dtos.takeaway.TakeAwaySummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface ITakeAwayOrderService
{
    TakeAwayOrderDTO placeOrder(AuthenticatedUser authUser, TakeAwayOrderCreateDTO dto);

    TakeAwayOrderDTO markAsPaid(AuthenticatedUser authUser, Long orderId);

    TakeAwayOrderDTO cancelOrder(AuthenticatedUser authUser, Long orderId);

    TakeAwayOrderDTO getById(Long orderId);

    List<TakeAwayOrderDTO> getOrdersByOffer(Long offerId);

    List<TakeAwayOrderDTO> getOrdersByDate(LocalDate date);

    TakeAwaySummaryDTO getSummary(LocalDate date);
}
