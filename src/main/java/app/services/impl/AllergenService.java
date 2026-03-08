package app.services.impl;

import app.dtos.allergen.AllergenCreateRequestDTO;
import app.dtos.allergen.AllergenDTO;
import app.dtos.allergen.AllergenUpdateRequestDTO;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.mappers.AllergenMapper;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.daos.interfaces.IUserReader;
import app.persistence.entities.Allergen;
import app.persistence.entities.User;
import app.services.IAllergenService;
import app.utils.EUAllergens;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AllergenService implements IAllergenService
{
    private final IAllergenDAO allergenDAO;
    private final IUserReader userReader;

    public AllergenService(IAllergenDAO allergenDAO, IUserReader userReader)
    {
        this.allergenDAO = allergenDAO;
        this.userReader = userReader;
    }

    @Override
    public AllergenDTO registerAllergen(Long creatorId, AllergenCreateRequestDTO dto)
    {
        ValidationUtil.validateId(creatorId);
        validateCreateInput(dto);

        User creator = userReader.getByID(creatorId);
        requireHeadChef(creator);

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
    public AllergenDTO updateAllergen(Long allergenId, Long editorId, AllergenUpdateRequestDTO dto)
    {
        ValidationUtil.validateId(allergenId);
        ValidationUtil.validateId(editorId);
        validateUpdateInput(dto);

        User editor = userReader.getByID(editorId);
        requireHeadChef(editor);

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
    public boolean deleteAllergen(Long allergenId, Long requesterId)
    {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(allergenId);

        User user = userReader.getByID(requesterId);
        requireHeadChef(user);

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
    public Set<AllergenDTO> getAllAllergens()
    {
        return allergenDAO.getAll().stream()
            .map(AllergenMapper::toDTO)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public AllergenDTO getAllergenByNameDA(String nameDA)
    {
        ValidationUtil.validateNotBlank(nameDA, "Name");

        return allergenDAO.findByNameDA(nameDA)
            .map(AllergenMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Allergen not found: " + nameDA));
    }

    @Override
    public List<AllergenDTO> seedEUAllergens(Long headChefId)
    {
        ValidationUtil.validateId(headChefId);

        User headChef = userReader.getByID(headChefId);
        requireHeadChef(headChef);

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
            throw new ValidationException("Allergen with Danish name '" + nameDA + "' already exists");
        }
    }

    private void requireUniqueNameEN(String nameEN)
    {
        if (allergenDAO.findByNameEN(nameEN).isPresent())
        {
            throw new ValidationException("Allergen with English name '" + nameEN + "' already exists");
        }
    }

    private void requireUniqueDisplayNumber(Integer displayNumber)
    {
        if (allergenDAO.existsByDisplayNumber(displayNumber))
        {
            throw new ValidationException("Display number already in use");
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

    private void requireHeadChef(User user)
    {
        if (!user.isHeadChef())
        {
            throw new UnauthorizedActionException("Only head chef can manage allergens");
        }
    }
}
