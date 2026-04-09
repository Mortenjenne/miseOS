package app.mappers;

import app.dtos.dish.DishReferenceDTO;
import app.dtos.takeaway.TakeAwayOfferDTO;
import app.dtos.takeaway.TakeAwayOfferReferenceDTO;
import app.dtos.takeaway.TakeAwayOfferSummaryDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.TakeAwayOffer;

public class TakeAwayOfferMapper
{
    private TakeAwayOfferMapper(){}

    public static TakeAwayOfferDTO toDTO(TakeAwayOffer takeAwayOffer)
    {
        DishReferenceDTO dishReferenceDTO = DishMapper.toDishReferenceDTO(takeAwayOffer.getDish());
        UserReferenceDTO userReferenceDTO = UserMapper.toReferenceDTO(takeAwayOffer.getCreatedBy());

        return new TakeAwayOfferDTO(
            takeAwayOffer.getId(),
            takeAwayOffer.isEnabled(),
            takeAwayOffer.isSoldOut(),
            takeAwayOffer.getOfferedPortions(),
            takeAwayOffer.getAvailablePortions(),
            takeAwayOffer.getPrice(),
            dishReferenceDTO,
            userReferenceDTO,
            takeAwayOffer.getCreatedAt(),
            takeAwayOffer.getUpdatedAt()
        );
    }

    public static TakeAwayOfferReferenceDTO toReferenceDTO(TakeAwayOffer takeAwayOffer)
    {
        if(takeAwayOffer == null)
        {
            return null;
        }

        return new TakeAwayOfferReferenceDTO(
            takeAwayOffer.getId(),
            takeAwayOffer.getDish().getNameDA(),
            takeAwayOffer.getPrice()
        );
    }

    public static TakeAwayOfferSummaryDTO toSummaryDTO(TakeAwayOffer offer)
    {
        DishReferenceDTO dishReferenceDTO = DishMapper.toDishReferenceDTO(offer.getDish());

        return new TakeAwayOfferSummaryDTO(
            offer.getId(),
            dishReferenceDTO,
            offer.getOfferedPortions(),
            offer.getTotalSoldPortions(),
            offer.getAvailablePortions(),
            offer.getTotalRevenue()
        );
    }

}
