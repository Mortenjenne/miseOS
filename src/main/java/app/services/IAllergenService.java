package app.services;

import app.dtos.allergen.AllergenCreateRequestDTO;
import app.dtos.allergen.AllergenDTO;
import app.dtos.allergen.AllergenUpdateRequestDTO;

import java.util.List;

public interface IAllergenService
{
    AllergenDTO registerAllergen(AllergenCreateRequestDTO dto);

    AllergenDTO updateAllergen(Long allergenId, AllergenUpdateRequestDTO dto);

    boolean deleteAllergen(Long allergenId);

    AllergenDTO getAllergenById(Long id);

    List<AllergenDTO> getAllAllergens();

    AllergenDTO getAllergenByNameDA(String nameDA);

    List<AllergenDTO> seedEUAllergens();
}
