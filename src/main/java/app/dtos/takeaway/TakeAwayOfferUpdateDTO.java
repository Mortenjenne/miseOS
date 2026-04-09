package app.dtos.takeaway;

public record TakeAwayOfferUpdateDTO(
    Long dishId,
    int offeredPortions,
    double price
)
{

}
