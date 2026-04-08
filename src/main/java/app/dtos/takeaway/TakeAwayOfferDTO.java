package app.dtos.takeaway;

import app.dtos.dish.DishReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TakeAwayOfferDTO(
    Long id,
    boolean enabled,
    boolean soldOut,
    int offeredPortions,
    int availablePortions,
    double price,
    DishReferenceDTO dish,
    UserReferenceDTO createdBy,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime updatedAt
) {}
