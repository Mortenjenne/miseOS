package app.services.impl;

import app.dtos.security.AuthenticatedUser;
import app.dtos.takeaway.TakeAwayOfferSummaryDTO;
import app.dtos.takeaway.TakeAwayOrderCreateDTO;
import app.dtos.takeaway.TakeAwayOrderDTO;
import app.dtos.takeaway.TakeAwaySummaryDTO;
import app.enums.OrderStatus;
import app.exceptions.UnauthorizedActionException;
import app.mappers.TakeAwayOfferMapper;
import app.mappers.TakeAwayOrderMapper;
import app.persistence.daos.interfaces.ITakeAwayOfferDAO;
import app.persistence.daos.interfaces.ITakeAwayOrderDAO;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.TakeAwayOffer;
import app.persistence.entities.TakeAwayOrder;
import app.persistence.entities.User;
import app.services.ITakeAwayOrderService;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class TakeAwayOrderService implements ITakeAwayOrderService
{
    private final ITakeAwayOrderDAO takeAwayOrderDAO;
    private final ITakeAwayOfferDAO takeAwayOfferDAO;
    private final IUserReader userReader;

    public TakeAwayOrderService(ITakeAwayOrderDAO takeAwayOrderDAO, ITakeAwayOfferDAO takeAwayOfferDAO, IUserReader userReader)
    {
        this.takeAwayOrderDAO = takeAwayOrderDAO;
        this.takeAwayOfferDAO = takeAwayOfferDAO;
        this.userReader = userReader;
    }

    @Override
    public TakeAwayOrderDTO placeOrder(AuthenticatedUser authUser, TakeAwayOrderCreateDTO dto)
    {
        validateAuthenticatedUser(authUser);
        validateCreateInput(dto);

        TakeAwayOrder savedOrder = takeAwayOrderDAO.create(authUser.userId(), dto);
        return TakeAwayOrderMapper.toDTO(savedOrder);
    }

    @Override
    public TakeAwayOrderDTO markAsPaid(AuthenticatedUser authUser, Long orderId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(orderId);

        User requester = userReader.getByID(authUser.userId());

        TakeAwayOrder takeAwayOrder = takeAwayOrderDAO.getByID(orderId);
        takeAwayOrder.setOrderPaid(requester);

        TakeAwayOrder updatedOrder = takeAwayOrderDAO.update(takeAwayOrder);
        return TakeAwayOrderMapper.toDTO(updatedOrder);
    }

    @Override
    public TakeAwayOrderDTO cancelOrder(AuthenticatedUser authUser, Long orderId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(orderId);

        User requester = userReader.getByID(authUser.userId());

        TakeAwayOrder takeAwayOrder = takeAwayOrderDAO.getByID(orderId);
        validateCancellationTime(requester, takeAwayOrder);
        takeAwayOrder.cancelOrder(requester);


        takeAwayOrder.getOrderLines().forEach(line ->
        {
            TakeAwayOffer offer = takeAwayOfferDAO.getByID(line.getTakeAwayOffer().getId());
            offer.addPortionsBack(line.getQuantity());
            takeAwayOfferDAO.update(offer);
        });

        TakeAwayOrder updatedOrder = takeAwayOrderDAO.update(takeAwayOrder);
        return TakeAwayOrderMapper.toDTO(updatedOrder);
    }

    @Override
    public TakeAwayOrderDTO getById(AuthenticatedUser authUser, Long orderId)
    {
        ValidationUtil.validateId(orderId);

        TakeAwayOrder takeAwayOrder = takeAwayOrderDAO.getByID(orderId);
        User requester = userReader.getByID(authUser.userId());

        validateOrderIdIsCustomers(authUser, takeAwayOrder, requester);

        return TakeAwayOrderMapper.toDTO(takeAwayOrder);
    }

    private void validateOrderIdIsCustomers(AuthenticatedUser authUser, TakeAwayOrder takeAwayOrder, User requester)
    {
        boolean isCustomerOrder = takeAwayOrder.getCustomer().getId().equals(authUser.userId());
        boolean isManagement = requester.isHeadChef() || requester.isSousChef();

        if (!isCustomerOrder && !isManagement)
        {
            throw new UnauthorizedActionException("You cannot view other customers orders.");
        }
    }

    @Override
    public List<TakeAwayOrderDTO> getOrders(AuthenticatedUser authUser, Long customerId, Long offerId, LocalDate date, OrderStatus status)
    {
        validateAuthenticatedUser(authUser);
        User requester = userReader.getByID(authUser.userId());

        Long searchCustomerId = getSearchCustomerId(requester, customerId);

        return takeAwayOrderDAO.findByFilter(searchCustomerId, offerId, date, status)
            .stream()
            .map(TakeAwayOrderMapper::toDTO)
            .toList();
    }

    @Override
    public TakeAwaySummaryDTO getSummary(LocalDate date)
    {
        LocalDate fallBackDate = date != null ? date : LocalDate.now();

        Set<TakeAwayOffer> takeAwayOffers = takeAwayOfferDAO.findByFilter(fallBackDate, null, null, null);

        Long totalOrders = takeAwayOrderDAO
            .countOrdersByDate(fallBackDate)
            .orElse(0L);

        List<TakeAwayOfferSummaryDTO> takeAwayOfferSummaryDTOS = takeAwayOffers
            .stream()
            .map(TakeAwayOfferMapper::toSummaryDTO)
            .toList();

        int totalOfferedPortions = takeAwayOffers
            .stream()
            .mapToInt(TakeAwayOffer::getOfferedPortions)
            .sum();

        int totalRemainingPortions = takeAwayOffers
            .stream()
            .mapToInt(TakeAwayOffer::getAvailablePortions)
            .sum();

        Long totalSoldPortions = takeAwayOrderDAO.sumSoldQuantityByDate(fallBackDate)
            .orElse(0L);

        return new TakeAwaySummaryDTO(
            date,
            totalOfferedPortions,
            totalSoldPortions,
            totalRemainingPortions,
            totalOrders,
            takeAwayOfferSummaryDTOS
        );
    }

    private Long getSearchCustomerId(User requester, Long requestedCustomerId)
    {
        if (requester.isHeadChef() || requester.isSousChef())
        {
            return requestedCustomerId;
        }

        return requester.getId();
    }

    private void validateCreateInput(TakeAwayOrderCreateDTO dto)
    {
        int minimumQuantityPerOrder = 1;
        int maximumQuantityPerOrder = 50;

        ValidationUtil.validateNotNull(dto, "Takeaway Order Create");
        ValidationUtil.validateNotEmpty(dto.takeAwayOrderLines(), "Order Lines");

        dto.takeAwayOrderLines().forEach(orderLine ->
        {
            ValidationUtil.validateId(orderLine.offerId());
            ValidationUtil.validateRange(orderLine.quantity(), minimumQuantityPerOrder, maximumQuantityPerOrder, "Quantity");
        });
    }

    private void validateAuthenticatedUser(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
    }

    private  void validateCancellationTime(User requester, TakeAwayOrder takeAwayOrder)
    {
        int cancellationTimeInMinuttes = 45;

        if (!requester.isHeadChef() && !requester.isSousChef())
        {
            boolean isCancellationWindowExpired = takeAwayOrder.getOrderedAt().isBefore(LocalDateTime.now().minusMinutes(cancellationTimeInMinuttes));
            if (isCancellationWindowExpired)
            {
                throw new UnauthorizedActionException("Cancellation window (45 mins) has expired. Please contact the kitchen.");
            }
        }
    }
}
