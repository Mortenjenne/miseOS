package app.services.impl;

import app.dtos.security.AuthenticatedUser;
import app.dtos.takeaway.TakeAwayOfferCreateDTO;
import app.dtos.takeaway.TakeAwayOfferDTO;
import app.dtos.takeaway.TakeAwayOfferUpdateDTO;
import app.exceptions.ConflictException;
import app.mappers.TakeAwayOfferMapper;
import app.persistence.daos.interfaces.ITakeAwayOfferDAO;
import app.persistence.daos.interfaces.readers.IDishReader;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.Dish;
import app.persistence.entities.TakeAwayOffer;
import app.persistence.entities.User;
import app.services.ITakeAwayOfferService;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
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

        boolean isDishUsedInTakeAwayOffer = takeAwayOfferDAO.existsByDishAndDate(dto.dishId(), LocalDate.now());

        if (LocalTime.now().isBefore(LocalTime.NOON))
        {
            throw new ConflictException("Takeaway offers can only be created after 12:00");
        }

        if (isDishUsedInTakeAwayOffer)
        {
            throw new ConflictException("A takeaway offer for this dish already exists today");
        }

        User createdBy = userReader.getByID(authUser.userId());
        Dish dish = dishReader.getByID(dto.dishId());

        TakeAwayOffer takeAwayOffer = new TakeAwayOffer(
            dto.offeredPortions(),
            dto.price(),
            createdBy,
            dish
        );

        TakeAwayOffer createdOffer = takeAwayOfferDAO.create(takeAwayOffer);
        return TakeAwayOfferMapper.toDTO(createdOffer);
    }

    @Override
    public TakeAwayOfferDTO getById(Long offerId)
    {
        ValidationUtil.validateId(offerId);

        TakeAwayOffer takeAwayOffer = takeAwayOfferDAO.getByID(offerId);
        return TakeAwayOfferMapper.toDTO(takeAwayOffer);
    }

    @Override
    public TakeAwayOfferDTO updateOffer(Long offerId, TakeAwayOfferUpdateDTO dto)
    {
        ValidationUtil.validateId(offerId);
        validateUpdateInput(dto);

        Dish dish = dishReader.getByID(dto.dishId());
        TakeAwayOffer takeAwayOffer = takeAwayOfferDAO.getByID(offerId);

        takeAwayOffer.updateOffer(
            dish,
            dto.offeredPortions(),
            dto.price()
        );

        TakeAwayOffer updatedOffer = takeAwayOfferDAO.update(takeAwayOffer);
        return TakeAwayOfferMapper.toDTO(updatedOffer);
    }

    @Override
    public TakeAwayOfferDTO enableOffer(AuthenticatedUser authUser, Long offerId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(offerId);

        TakeAwayOffer takeAwayOffer = takeAwayOfferDAO.getByID(offerId);
        takeAwayOffer.enableOffer();

        TakeAwayOffer updatedOffer = takeAwayOfferDAO.update(takeAwayOffer);
        return TakeAwayOfferMapper.toDTO(updatedOffer);
    }

    @Override
    public TakeAwayOfferDTO disableOffer(AuthenticatedUser authUser, Long offerId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(offerId);

        TakeAwayOffer takeAwayOffer = takeAwayOfferDAO.getByID(offerId);
        takeAwayOffer.disableOffer();

        TakeAwayOffer updatedOffer = takeAwayOfferDAO.update(takeAwayOffer);
        return TakeAwayOfferMapper.toDTO(updatedOffer);
    }

    @Override
    public List<TakeAwayOfferDTO> getOffers(LocalDate date, Boolean isSoldOut, Boolean isEnabled, Long dishId)
    {
        return takeAwayOfferDAO.findByFilter(date, isSoldOut, isEnabled, dishId)
            .stream()
            .map(TakeAwayOfferMapper::toDTO)
            .toList();
    }

    @Override
    public boolean deleteOffer(AuthenticatedUser authUser, Long offerId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(offerId);

        boolean isOfferInUse = takeAwayOfferDAO.isUsedInAnyOrders(offerId);

        if (isOfferInUse)
        {
            throw new ConflictException("Cant delete Take away offer - already sold");
        }

        return takeAwayOfferDAO.delete(offerId);
    }

    private void validateCreateInput(TakeAwayOfferCreateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Takeaway Offer Create");
        ValidationUtil.validateId(dto.dishId());
        ValidationUtil.validateRange(dto.offeredPortions(), 1, 1000, "Offered portions");
        ValidationUtil.validatePositive(dto.price(), "Take away price");
    }

    private void validateUpdateInput(TakeAwayOfferUpdateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Take away offer update");
        ValidationUtil.validateRange(dto.offeredPortions(), 1, 1000, "Offered portions");
        ValidationUtil.validatePositive(dto.price(), "Take away price");
    }

    private void validateAuthenticatedUser(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
    }
}
