package app.dtos.dish;

import java.util.Set;

public record DishUpdateRequestDTO(
    Long id,
    Long editorId,
    String nameDA,
    String nameEN,
    String descriptionDA,
    String descriptionEN,
    Set<Long> allergenIds
) {}
