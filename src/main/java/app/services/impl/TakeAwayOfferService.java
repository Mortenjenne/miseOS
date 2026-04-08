package app.services.impl;

import app.dtos.security.AuthenticatedUser;
import app.dtos.takeaway.TakeAwayOfferCreateDTO;
import app.dtos.takeaway.TakeAwayOfferDTO;
import app.dtos.takeaway.TakeAwayOfferUpdateDTO;
import app.persistence.daos.interfaces.ITakeAwayOfferDAO;
import app.persistence.daos.interfaces.readers.IDishReader;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.Dish;
import app.persistence.entities.TakeAwayOffer;
import app.persistence.entities.User;
import app.services.ITakeAwayOfferService;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class TakeAwayOfferService implements ITakeAwayOfferService
{
    private final ITakeAwayOfferDAO takeAwayOfferDAO;
    private final IUserReader userReader;
    private final IDishReader dishReader;

    public TakeAwayOfferService(ITakeAwayOfferDAO takeAwayOfferDAO, IUserReader userReader, IDishReader dishReader)
    {
        this.takeAwayOfferDAO = takeAwayOfferDAO;
        this.userReader = userReader;
        this.dishReader = dishReader;
    }

    @Override
    public TakeAwayOfferDTO createOffer(AuthenticatedUser authUser, TakeAwayOfferCreateDTO dto)
    {
        validateAuthenticatedUser(authUser);
        validateCreateInput(dto);

        User createdBy = userReader.getByID(authUser.userId());
        Dish dish = dishReader.getByID(dto.dishId());

        TakeAwayOffer takeAwayOffer = new TakeAwayOffer(
            dto.offeredPortions(),
            dto.price(),
            createdBy,
            dish
        );

        TakeAwayOffer createdOffer = takeAwayOfferDAO.create(takeAwayOffer);
        return null;
    }

    @Override
    public TakeAwayOfferDTO getById(Long offerId)
    {
        return null;
    }

    @Override
    public TakeAwayOfferDTO updateOffer(Long offerId, TakeAwayOfferUpdateDTO dto)
    {
        return null;
    }

    @Override
    public TakeAwayOfferDTO enableOffer(AuthenticatedUser authUser, Long offerId)
    {
        return null;
    }

    @Override
    public TakeAwayOfferDTO disableOffer(AuthenticatedUser authUser, Long offerId)
    {
        return null;
    }

    @Override
    public List<TakeAwayOfferDTO> getOffers(LocalDate date, Boolean onlyActive, Long dishId)
    {
        return List.of();
    }

    @Override
    public boolean deleteOffer(AuthenticatedUser authUser, Long offerId)
    {
        return false;
    }

    private void validateCreateInput(TakeAwayOfferCreateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Takeaway Offer Create");
        ValidationUtil.validateId(dto.dishId());
        ValidationUtil.validateRange(dto.offeredPortions(), 1, 1000, "Offered portions");
        ValidationUtil.validatePositive(dto.price(), "Take away price");
    }

    private void validateAuthenticatedUser(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
    }
}
