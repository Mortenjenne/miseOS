package app.dtos.dish;

import java.util.Set;

public record DishSuggestionUpdateDTO(
    String nameDA,
    String descriptionDA,
    Set<Long> allergenIds
) {}
