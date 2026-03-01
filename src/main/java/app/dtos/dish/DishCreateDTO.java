package app.dtos.dish;

import java.util.Set;

public record DishCreateDTO(
    String nameDA,
    String descriptionDA,
    Long stationId,
    Set<Long> allergenIds
)
{
}
