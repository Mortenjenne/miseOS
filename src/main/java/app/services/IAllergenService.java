package app.services;

import app.dtos.allergen.AllergenCreateRequestDTO;
import app.dtos.allergen.AllergenDTO;
import app.dtos.allergen.AllergenUpdateRequestDTO;

import java.util.List;
import java.util.Set;

public interface IAllergenService
{
    AllergenDTO registerAllergen(Long creatorId, AllergenCreateRequestDTO dto);

    AllergenDTO updateAllergen(Long allergenId, Long editorId, AllergenUpdateRequestDTO dto);

    boolean deleteAllergen(Long allergenId, Long requesterId);

    AllergenDTO getAllergenById(Long id);

    Set<AllergenDTO> getAllAllergens();

    AllergenDTO getAllergenByNameDA(String nameDA);

    List<AllergenDTO> seedEUAllergens(Long headChefId);
}
