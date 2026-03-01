package app.dtos.dish;

import java.util.Set;

public record DishCreateDTO(
    String nameDA,
    String descriptionDA,
    Long stationId,
    String stationName,
    Set<String> allergens
)
{
}
