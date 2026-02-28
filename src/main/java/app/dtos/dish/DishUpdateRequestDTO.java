package app.dtos.dish;

import java.util.Set;

public record DishUpdateRequestDTO(
    String nameDA,
    String descriptionDA,
    Set<Long> allergenIds
) {}
