package app.services;

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
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AllergenService
{

    private final IAllergenDAO allergenDAO;
    private final IUserReader userReader;

    public AllergenService(IAllergenDAO allergenDAO, IUserReader userReader)
    {
        this.allergenDAO = allergenDAO;
        this.userReader = userReader;
    }

    public AllergenDTO registerAllergen(AllergenCreateRequestDTO dto)
    {
        User creator = userReader.getByID(dto.createdById());

        requireHeadChef(creator);

        validateAllergenNameUnique(dto.name());

        Allergen allergen = new Allergen(
            dto.name(),
            dto.description(),
            dto.displayNumber()
        );

        Allergen saved = allergenDAO.create(allergen);

        return AllergenMapper.toDTO(saved);
    }

    public AllergenDTO updateAllergen(Long allergenId, AllergenUpdateRequestDTO dto)
    {
        Allergen allergen = allergenDAO.getByID(allergenId);
        User editor = userReader.getByID(dto.editorId());

        requireHeadChef(editor);

        if (!allergen.getName().equals(dto.name()))
        {
            validateAllergenNameUnique(dto.name());
        }

        allergen.update(
            dto.name(),
            dto.description(),
            dto.displayNumber()
        );

        Allergen updated = allergenDAO.update(allergen);

        return AllergenMapper.toDTO(updated);
    }

    public boolean deleteAllergen(Long allergenId, Long userId)
    {
        Allergen allergen = allergenDAO.getByID(allergenId);
        User user = userReader.getByID(userId);

        requireHeadChef(user);

        // TODO: Check if allergen is in use

        return allergenDAO.delete(allergen.getId());
    }

    public AllergenDTO getAllergenById(Long id)
    {
        Allergen allergen = allergenDAO.getByID(id);
        return AllergenMapper.toDTO(allergen);
    }

    public Set<AllergenDTO> getAllAllergens()
    {
        return allergenDAO.getAll().stream()
            .map(AllergenMapper::toDTO)
            .collect(Collectors.toSet());
    }

    public AllergenDTO getAllergenByName(String name)
    {
        return findAllergenByName(name)
            .orElseThrow(() -> new EntityNotFoundException("Allergen not found: " + name));
    }

    private Optional<AllergenDTO> findAllergenByName(String name)
    {
        ValidationUtil.validateNotBlank(name, "Name");

        return allergenDAO.findByName(name)
            .map(AllergenMapper::toDTO);
    }

    //TODO implement!
    public void seedEUAllergens(Long headChefId)
    {
        User headChef = userReader.getByID(headChefId);
        requireHeadChef(headChef);

        String[] euAllergens = {
            "Gluten",           // Cereals containing gluten
            "Crustaceans",      // Crustaceans and products thereof
            "Eggs",             // Eggs and products thereof
            "Fish",             // Fish and products thereof
            "Peanuts",          // Peanuts and products thereof
            "Soybeans",         // Soybeans and products thereof
            "Milk",             // Milk and products thereof (including lactose)
            "Nuts",             // Tree nuts
            "Celery",           // Celery and products thereof
            "Mustard",          // Mustard and products thereof
            "Sesame",           // Sesame seeds and products thereof
            "Sulphites",        // Sulphur dioxide and sulphites
            "Lupin",            // Lupin and products thereof
            "Molluscs"          // Molluscs and products thereof
        };

        for (String name : euAllergens)
        {
            if (allergenDAO.findByName(name).isEmpty())
            {
                //Allergen allergen = new Allergen(name);
                //allergenDAO.create(allergen);
            }
        }
    }

    private void requireHeadChef(User user)
    {
        if (!user.isHeadChef())
        {
            throw new UnauthorizedActionException("Only head chef can manage allergens");
        }
    }

    private void validateAllergenNameUnique(String name)
    {
        Optional<Allergen> existing = allergenDAO.findByName(name);

        if (existing.isPresent())
        {
            throw new ValidationException("Allergen with name '" + name + "' already exists");
        }
    }
}
