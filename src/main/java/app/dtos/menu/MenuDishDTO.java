package app.dtos.menu;

import app.dtos.allergen.AllergenDTO;

import java.util.List;

public record MenuDishDTO(
    Long id,
    String nameDA,
    String nameEN,
    String descriptionDA,
    String descriptionEN,
    Boolean hasTranslation,
    List<AllergenDTO> allergens
) {}
