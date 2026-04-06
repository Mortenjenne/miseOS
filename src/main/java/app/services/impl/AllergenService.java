package app.services.impl;

import app.dtos.allergen.AllergenCreateRequestDTO;
import app.dtos.allergen.AllergenDTO;
import app.dtos.allergen.AllergenUpdateRequestDTO;
import app.exceptions.ConflictException;
import app.exceptions.ValidationException;
import app.mappers.AllergenMapper;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.entities.Allergen;
import app.services.IAllergenService;
import app.utils.EUAllergens;
import app.utils.ValidationUtil;

import java.util.*;

public class AllergenService implements IAllergenService
{
    private final IAllergenDAO allergenDAO;

    public AllergenService(IAllergenDAO allergenDAO)
    {
        this.allergenDAO = allergenDAO;
    }

    @Override
    public AllergenDTO registerAllergen(AllergenCreateRequestDTO dto)
    {
        validateCreateInput(dto);
        requireUniqueNameDA(dto.nameDA());
        requireUniqueNameEN(dto.nameEN());
        requireUniqueDisplayNumber(dto.displayNumber());

        Allergen allergen = new Allergen(
            dto.nameDA(),
            dto.nameEN(),
            dto.descriptionDA(),
            dto.descriptionEN(),
            dto.displayNumber()
        );

        Allergen saved = allergenDAO.create(allergen);
        return AllergenMapper.toDTO(saved);
    }

    @Override
    public AllergenDTO updateAllergen(Long allergenId, AllergenUpdateRequestDTO dto)
    {
        ValidationUtil.validateId(allergenId);
        validateUpdateInput(dto);

        Allergen allergen = allergenDAO.getByID(allergenId);

        if (!allergen.getNameDA().equalsIgnoreCase(dto.nameDA()))
        {
            requireUniqueNameDA(dto.nameDA());
        }

        if (!allergen.getNameEN().equalsIgnoreCase(dto.nameEN()))
        {
            requireUniqueNameEN(dto.nameEN());
        }

        if(!allergen.getDisplayNumber().equals(dto.displayNumber()))
        {
            requireUniqueDisplayNumber(dto.displayNumber());
        }

        allergen.update(
            dto.nameDA(),
            dto.nameEN(),
            dto.descriptionDA(),
            dto.descriptionEN(),
            dto.displayNumber()
        );

        Allergen updated = allergenDAO.update(allergen);
        return AllergenMapper.toDTO(updated);
    }

    @Override
    public boolean deleteAllergen(Long allergenId)
    {
        ValidationUtil.validateId(allergenId);

        Allergen allergen = allergenDAO.getByID(allergenId);

        boolean isUsed = allergenDAO.isUsedByAnyDish(allergenId);
        if (isUsed)
        {
            throw new ValidationException("Cannot delete allergen '" + allergen.getNameDA() + ", it is used by one or more dishes");
        }

        return allergenDAO.delete(allergen.getId());
    }

    @Override
    public AllergenDTO getAllergenById(Long id)
    {
        ValidationUtil.validateId(id);
        Allergen allergen = allergenDAO.getByID(id);
        return AllergenMapper.toDTO(allergen);
    }

    @Override
    public List<AllergenDTO> getAllAllergens()
    {
        return allergenDAO.getAll().stream()
            .map(AllergenMapper::toDTO)
            .sorted(Comparator.comparing(AllergenDTO::displayNumber))
            .toList();
    }

    @Override
    public List<AllergenDTO> searchByName(String query)
    {
        ValidationUtil.validateNotBlank(query, "Search query");

        return allergenDAO.searchByName(query).stream()
            .map(AllergenMapper::toDTO)
            .toList();
    }

    @Override
    public List<AllergenDTO> seedEUAllergens()
    {
        long numberOfAllergensInDB = allergenDAO.count();

        if (numberOfAllergensInDB> 0)
        {
            throw new ValidationException("EU allergens already seeded");
        }

        List<AllergenDTO> seeded = new ArrayList<>();
        List<Allergen> euAllergens = EUAllergens.getAll();

        euAllergens.forEach(a ->
        {
            Allergen allergen = allergenDAO.create(a);
            AllergenDTO allergenDTO = AllergenMapper.toDTO(allergen);
            seeded.add(allergenDTO);
        });

        return seeded;
    }

    private void validateCreateInput(AllergenCreateRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Allergen");
        validateNames(dto.nameDA(), dto.nameEN());
        validateDescriptions(dto.descriptionDA(), dto.descriptionEN());
        validateDisplayNumber(dto.displayNumber());
    }

    private void validateUpdateInput(AllergenUpdateRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Allergen");
        validateNames(dto.nameDA(), dto.nameEN());
        validateDescriptions(dto.descriptionDA(), dto.descriptionEN());
        validateDisplayNumber(dto.displayNumber());
    }

    private void requireUniqueNameDA(String nameDA)
    {
        if (allergenDAO.findByNameDA(nameDA).isPresent())
        {
            throw new ConflictException("Allergen with Danish name '" + nameDA + "' already exists");
        }
    }

    private void requireUniqueNameEN(String nameEN)
    {
        if (allergenDAO.findByNameEN(nameEN).isPresent())
        {
            throw new ConflictException("Allergen with English name '" + nameEN + "' already exists");
        }
    }

    private void requireUniqueDisplayNumber(Integer displayNumber)
    {
        if (allergenDAO.existsByDisplayNumber(displayNumber))
        {
            throw new ConflictException("Display number already in use");
        }
    }

    private void validateNames(String nameDA, String nameEN)
    {
        ValidationUtil.validateName(nameDA, "Name DA");
        ValidationUtil.validateName(nameEN, "Name EN");
    }

    private void validateDescriptions(String descDA, String descEN)
    {
        ValidationUtil.validateDescription(descDA, "Description Da");
        ValidationUtil.validateDescription(descEN, "Description EN");
    }

    private void validateDisplayNumber(Integer displayNumber)
    {
        if (displayNumber == null)
        {
            throw new ValidationException("Display number is required");
        }

        if (displayNumber < 1 || displayNumber > 99)
        {
            throw new ValidationException("Display number must be between 1 and 99");
        }
    }
}
