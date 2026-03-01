package app.dtos.dish;

import app.dtos.allergen.AllergenDTO;

import java.time.LocalDateTime;
import java.util.Set;

public record DishDTO(
    Long id,
    String nameDA,
    String nameEN,
    String descriptionDA,
    String descriptionEN,
    Long stationId,
    String stationName,
    Set<AllergenDTO> allergens,
    boolean isActive,
    int originWeek,
    int originYear,
    LocalDateTime createdAt
)
{
}
