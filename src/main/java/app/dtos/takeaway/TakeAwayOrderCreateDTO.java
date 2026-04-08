package app.dtos.takeaway;

public record TakeAwayOrderCreateDTO(
    Long offerId,
    Long userId,
    int quantity
)
{
}
