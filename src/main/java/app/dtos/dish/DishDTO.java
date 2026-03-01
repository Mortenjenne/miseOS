package app.dtos.dish;

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
    Set<String> allergens,
    boolean isActive,
    int originWeek,
    int originYear,
    LocalDateTime createdAt
)
{
}
