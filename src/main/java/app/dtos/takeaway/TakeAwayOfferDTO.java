package app.dtos.takeaway;

import java.time.LocalDate;

public record TakeAwayOfferDTO(
    Long id,
    boolean enabled,
    boolean soldOut,
    int offeredPortions,
    int availablePortions,
    double price,
    LocalDate createdAt,
    Long dishId,
    String dishNameDA
) {}
