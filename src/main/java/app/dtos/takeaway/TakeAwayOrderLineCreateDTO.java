package app.dtos.takeaway;

public record TakeAwayOrderLineCreateDTO(
    Long offerId,
    int quantity
)
{
}
