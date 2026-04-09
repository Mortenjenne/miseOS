package app.dtos.takeaway;

import app.dtos.dish.DishReferenceDTO;

public record TakeAwayOfferSummaryDTO(
    Long offerId,
    DishReferenceDTO dish,
    int offeredPortions,
    int soldPortions,
    int remainingPortions,
    double revenue
) {
}
