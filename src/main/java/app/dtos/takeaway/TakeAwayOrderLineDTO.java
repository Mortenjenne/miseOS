package app.dtos.takeaway;

public record TakeAwayOrderLineDTO(
    Long id,
    TakeAwayOfferReferenceDTO offer,
    int quantity,
    double lineTotal
) {

}
