package app.mappers;

import app.dtos.allergen.AllergenDTO;
import app.persistence.entities.Allergen;

public class AllergenMapper
{
    private AllergenMapper() {}

    public static AllergenDTO toDTO(Allergen allergen)
    {
        return new AllergenDTO(
            allergen.getId(),
            allergen.getNameDA(),
            allergen.getNameEN(),
            allergen.getDescriptionDA(),
            allergen.getDescriptionEN(),
            allergen.getDisplayNumber()
        );
    }
}
