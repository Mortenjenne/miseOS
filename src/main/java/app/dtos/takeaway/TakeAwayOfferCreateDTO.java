package app.dtos.takeaway;

public record TakeAwayOfferCreateDTO(
    Long dishId,
    int offeredPortions,
    double price
) {}
