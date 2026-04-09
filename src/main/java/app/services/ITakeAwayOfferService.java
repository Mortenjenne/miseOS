package app.services;

import app.dtos.security.AuthenticatedUser;
import app.dtos.takeaway.TakeAwayOfferCreateDTO;
import app.dtos.takeaway.TakeAwayOfferDTO;
import app.dtos.takeaway.TakeAwayOfferUpdateDTO;

import java.time.LocalDate;
import java.util.List;

public interface ITakeAwayOfferService
{
    TakeAwayOfferDTO createOffer(AuthenticatedUser authUser, TakeAwayOfferCreateDTO dto);

    TakeAwayOfferDTO getById(Long offerId);

    TakeAwayOfferDTO updateOffer(Long offerId, TakeAwayOfferUpdateDTO dto);

    TakeAwayOfferDTO enableOffer(AuthenticatedUser authUser, Long offerId);

    TakeAwayOfferDTO disableOffer(AuthenticatedUser authUser, Long offerId);

    List<TakeAwayOfferDTO> getOffers(LocalDate date, Boolean isSoldOut, Boolean isEnabled, Long dishId);

    boolean deleteOffer(AuthenticatedUser authUser, Long offerId);
}
