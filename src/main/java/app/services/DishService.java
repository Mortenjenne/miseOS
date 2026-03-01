package app.services;

import app.dtos.dish.*;
import app.exceptions.UnauthorizedActionException;
import app.mappers.DishMapper;
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

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Map;
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

        LocalDate today = LocalDate.now();
        int currentWeek = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int currentYear = today.get(IsoFields.WEEK_BASED_YEAR);

        Dish dish = new Dish(
            dto.nameDA(),
            dto.descriptionDA(),
            station,
            allergens,
            creator,
            currentWeek,
            currentYear
        );

        Dish created = dishDAO.create(dish);
        return DishMapper.toDTO(created);
    }

    public DishDTO updateDish(Long editorId, Long dishId, DishUpdateDTO dto)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(dishId);
        validateUpdateInput(dto);

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
        return DishMapper.toDTO(updated);
    }

    public DishDTO getById(Long dishId)
    {
        ValidationUtil.validateId(dishId);
        Dish dish = dishDAO.getByID(dishId);
        return DishMapper.toDTO(dish);
    }

    public DishDTO getByIdWithAllergens(Long dishId)
    {
        ValidationUtil.validateId(dishId);

        return dishDAO.getByIdWithAllergens(dishId)
            .map(DishMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Dish with ID " + dishId + " not found"));
    }

    public Set<DishDTO> getAllActive()
    {
        return dishDAO.findAllActive()
            .stream()
            .map(DishMapper::toDTO)
            .collect(Collectors.toSet());
    }

    public Set<DishDTO> searchByName(String query)
    {
        ValidationUtil.validateNotBlank(query, "Search query");

        return dishDAO.searchByName(query)
            .stream()
            .map(DishMapper::toDTO)
            .collect(Collectors.toSet());
    }

    public boolean deleteDish(Long dishId, Long userId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        requireHeadChefOrSousChef(user);

        if (dishDAO.isUsedInAnyMenu(dishId))
        {
            throw new IllegalStateException("Dish is used in menus - use deactivate instead");
        }

        return dishDAO.delete(dishId);
    }

    public DishDTO deactivate(Long dishId, Long userId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        requireHeadChefOrSousChef(user);

        Dish dish = dishDAO.getByID(dishId);
        dish.deactivate();
        Dish updated = dishDAO.update(dish);
        return DishMapper.toDTO(updated);
    }

    public DishDTO activate(Long dishId, Long userId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        requireHeadChefOrSousChef(user);

        Dish dish = dishDAO.getByID(dishId);
        dish.activate();
        Dish updated = dishDAO.update(dish);
        return DishMapper.toDTO(dish);
    }

    public AvailableDishesDTO getAvailableDishesForMenu(int week, int year)
    {
        ValidationUtil.validateRange(week, 1, 53, "Week");
        ValidationUtil.validateRange(year, 2020, 2100, "Year");

        Set<Dish> newDishesFromThisWeek = dishDAO.findByOriginWeekAndYear(week, year);
        Set<Dish> fromDishHistory = dishDAO.findFromPreviousWeeks(week, year);

        return new AvailableDishesDTO(
            week,
            year,
            groupDishOptionsByStation(newDishesFromThisWeek),
            groupDishOptionsByStation(fromDishHistory)
        );
    }

    public Map<String, Set<DishOptionDTO>> getAllActiveDishesGrouped()
    {
        Set<Dish> dishes = dishDAO.findAllActive();
        return groupDishOptionsByStation(dishes);
    }

    private Map<String, Set<DishOptionDTO>> groupDishOptionsByStation(Set<Dish> dishes)
    {
        return dishes.stream()
            .collect(Collectors.groupingBy(
                d -> d.getStation().getStationName(),
                Collectors.mapping(DishMapper::toOptionDTO, Collectors.toSet())
            ));
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

    private void validateUpdateInput(DishUpdateDTO dto) {
        ValidationUtil.validateNotNull(dto, "Dish update");
        ValidationUtil.validateNotBlank(dto.nameDA(), "Name");
        ValidationUtil.validateNotBlank(dto.descriptionDA(), "Description");
    }

    private void requireHeadChefOrSousChef(User user)
    {
        if(!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef or sous chef can create dishes directly");
        }
    }
}
