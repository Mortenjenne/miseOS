package app.dtos.takeaway;

import app.dtos.user.UserReferenceDTO;

public record TakeAwayOrderDTO(
    Long id,
    Long offerId,
    UserReferenceDTO customer,
    int quantity,
    double totalPrice,
    String orderStatus
) {}
