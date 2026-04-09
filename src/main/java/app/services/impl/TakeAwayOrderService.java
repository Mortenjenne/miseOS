package app.services.impl;

import app.dtos.security.AuthenticatedUser;
import app.dtos.takeaway.TakeAwayOrderCreateDTO;
import app.dtos.takeaway.TakeAwayOrderDTO;
import app.dtos.takeaway.TakeAwaySummaryDTO;
import app.persistence.daos.interfaces.ITakeAwayOfferDAO;
import app.persistence.daos.interfaces.ITakeAwayOrderDAO;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.TakeAwayOffer;
import app.persistence.entities.TakeAwayOrder;
import app.persistence.entities.User;
import app.services.ITakeAwayOrderService;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

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

        TakeAwayOffer takeAwayOffer = takeAwayOfferDAO.getByID(dto.offerId());
        User customer = userReader.getByID(dto.userId());

        takeAwayOffer.sellPortions(dto.quantity());
        TakeAwayOffer updatedOffer = takeAwayOfferDAO.update(takeAwayOffer);

        TakeAwayOrder takeAwayOrder = new TakeAwayOrder(
            customer,
            updatedOffer,
            dto.quantity()
        );

        return Take
    }



    @Override
    public TakeAwayOrderDTO markAsPaid(AuthenticatedUser authUser, Long orderId)
    {
        return null;
    }

    @Override
    public TakeAwayOrderDTO cancelOrder(AuthenticatedUser authUser, Long orderId)
    {
        return null;
    }

    @Override
    public TakeAwayOrderDTO getById(Long orderId)
    {
        return null;
    }

    @Override
    public List<TakeAwayOrderDTO> getOrdersByOffer(Long offerId)
    {
        return List.of();
    }

    @Override
    public List<TakeAwayOrderDTO> getOrdersByDate(LocalDate date)
    {
        return List.of();
    }

    @Override
    public TakeAwaySummaryDTO getSummary(LocalDate date)
    {
        return null;
    }

    private void validateCreateInput(TakeAwayOrderCreateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Takeaway Order Create");
        ValidationUtil.validateId(dto.offerId());
        ValidationUtil.validateId(dto.userId());

        int minimumQuantityPerOrder = 1;
        int maximumQuantityPerOrder = 50;

        ValidationUtil.validateRange(dto.quantity(), minimumQuantityPerOrder, maximumQuantityPerOrder, "Quantity");
    }

    private void validateAuthenticatedUser(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
    }
}
