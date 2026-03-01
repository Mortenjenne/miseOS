package app.dtos.dish;

import java.util.Set;

public record DishUpdateDTO(
    String nameDA,
    String descriptionDA,
    String nameEN,
    String descriptionEN,
    Set<Long> allergenIds
)
{
}
