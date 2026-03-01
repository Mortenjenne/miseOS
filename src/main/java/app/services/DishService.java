package app.services;

import app.dtos.allergen.AllergenDTO;
import app.dtos.dish.DishCreateDTO;
import app.dtos.dish.DishDTO;
import app.dtos.dish.DishUpdateDTO;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.daos.interfaces.IDishDAO;
import app.persistence.daos.interfaces.IStationReader;
import app.persistence.daos.interfaces.IUserReader;
import app.persistence.entities.Allergen;
import app.persistence.entities.Dish;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

public class DishService
{
    private final IDishDAO dishDAO;
    private final IAllergenDAO allergenDAO;
    private final IStationReader stationReader;
    private final IUserReader userReader;


    public DishService(IDishDAO dishDAO, IAllergenDAO allergenDAO, IStationReader stationReader, IUserReader userReader)
    {
        this.dishDAO = dishDAO;
        this.allergenDAO = allergenDAO;
        this.stationReader = stationReader;
        this.userReader = userReader;
    }

    public DishDTO createDish(Long creatorId, DishCreateDTO dto)
    {
        ValidationUtil.validateId(creatorId);
        ValidationUtil.validateId(dto.stationId());
        validateCreateInput(dto);

        User creator = userReader.getByID(creatorId);
        requireHeadChefOrSousChef(creator);
        Station station = stationReader.getByID(dto.stationId());
        Set<Allergen> allergens = fetchAllergens(dto.allergenIds());

        Dish dish = new Dish(
            dto.nameDA(),
            dto.descriptionDA(),
            station,
            allergens,
            creator,
            1, //TODO what to do if its a direct entry from head chef sous chef?
            2 //TODO what to do if its a direct entry from head chef sous chef?
        );

        Dish created = dishDAO.create(dish);
        return mapToDishDTO(created);
    }

    public DishDTO updateDish(Long editorId, Long dishId, DishUpdateDTO dto)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(dishId);

        User editor = userReader.getByID(editorId);
        requireHeadChefOrSousChef(editor);

        Dish dish = dishDAO.getByID(dishId);
        Set<Allergen> allergens = fetchAllergens(dto.allergenIds());

        dish.update(
            dto.nameDA(),
            dto.descriptionDA(),
            dto.nameEN(),
            dto.descriptionEN(),
            allergens
        );

        Dish updated = dishDAO.update(dish);
        return mapToDishDTO(updated);
    }

    public DishDTO getById(Long dishId)
    {
        ValidationUtil.validateId(dishId);
        Dish dish = dishDAO.getByID(dishId);
        return mapToDishDTO(dish);
    }

    public DishDTO getByIdWithAllergens(Long dishId)
    {
        ValidationUtil.validateId(dishId);

        return dishDAO.getByIdWithAllergens(dishId)
            .map(this::mapToDishDTO)
            .orElseThrow(() -> new EntityNotFoundException("Dish with ID " + dishId + " not found"));
    }

    public Set<DishDTO> getAllActive()
    {
        return dishDAO.findAllActive()
            .stream()
            .map(this::mapToDishDTO)
            .collect(Collectors.toSet());
    }

    public Set<DishDTO> searchByName(String query)
    {
        ValidationUtil.validateNotBlank(query, "Search query");

        return dishDAO.searchByName(query)
            .stream()
            .map(this::mapToDishDTO)
            .collect(Collectors.toSet());
    }

    //TODO Soft delete or Hard delete? do i need delete in dao then

    public void deactivate(Long dishId, Long userId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        requireHeadChefOrSousChef(user);

        Dish dish = dishDAO.getByID(dishId);
        dish.deactivate();
        dishDAO.update(dish);
    }

    public void activate(Long dishId, Long userId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        requireHeadChefOrSousChef(user);

        Dish dish = dishDAO.getByID(dishId);
        dish.activate();
        dishDAO.update(dish);
    }

    private Set<Allergen> fetchAllergens(Set<Long> allergenIds)
    {
        if (allergenIds == null || allergenIds.isEmpty()) {
            return Set.of();
        }

        return allergenIds.stream()
            .map(allergenDAO::getByID)
            .collect(Collectors.toSet());
    }

    private void validateCreateInput(DishCreateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Create input");
        ValidationUtil.validateNotBlank(dto.nameDA(), "Dish name");
        ValidationUtil.validateNotBlank(dto.descriptionDA(), "Dish description");
    }

    private void requireHeadChefOrSousChef(User user)
    {
        if(!user.isHeadChef() || !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef or sous chef can create dishes directly");
        }
    }

    private DishDTO mapToDishDTO(Dish dish)
    {
        return new DishDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getNameEN(),
            dish.getDescriptionDA(),
            dish.getDescriptionEN(),
            dish.getStation().getId(),
            dish.getStation().getStationName(),
            dish.getAllergens().stream().map(Allergen::getName).collect(Collectors.toSet()),
            dish.isActive(),
            dish.getOriginWeek(),
            dish.getOriginYear(),
            dish.getCreatedAt()
        );
    }
}
