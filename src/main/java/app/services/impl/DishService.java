package app.services.impl;

import app.dtos.dish.*;
import app.dtos.security.AuthenticatedUser;
import app.exceptions.ConflictException;
import app.mappers.DishMapper;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.daos.interfaces.IDishDAO;
import app.persistence.daos.interfaces.readers.IStationReader;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.Allergen;
import app.persistence.entities.Dish;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.services.IDishService;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DishService implements IDishService
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

    @Override
    public DishDTO createDish(AuthenticatedUser authUser, DishCreateDTO dto)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
        validateCreateInput(dto);

        User creator = userReader.getByID(authUser.userId());
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

    @Override
    public DishDTO updateDish(Long dishId, DishUpdateDTO dto)
    {
        ValidationUtil.validateId(dishId);
        validateUpdateInput(dto);

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

    @Override
    public DishDetailDTO getById(Long dishId)
    {
        ValidationUtil.validateId(dishId);

        Dish dish = dishDAO.getByID(dishId);
        int menuCount = dishDAO.countMenuUsage(dishId);
        String lastServed = dishDAO.findLastServed(dishId).orElse(null);
        return DishMapper.toDetailDTO(dish, menuCount, lastServed);
    }

    @Override
    public List<DishDTO> searchByName(String query)
    {
        ValidationUtil.validateNotBlank(query, "Search query");
        ValidationUtil.validateRange(query.trim().length(), 2, 100, "Search query length");

        return dishDAO.searchByName(query)
            .stream()
            .map(DishMapper::toDTO)
            .toList();
    }

    @Override
    public boolean deleteDish(Long dishId)
    {
        ValidationUtil.validateId(dishId);

        if (dishDAO.isUsedInAnyMenu(dishId))
        {
            throw new ConflictException("Dish is used in menus - use deactivate instead");
        }

        return dishDAO.delete(dishId);
    }

    @Override
    public DishDTO deactivate(Long dishId)
    {
        ValidationUtil.validateId(dishId);

        Dish dish = dishDAO.getByID(dishId);
        dish.deactivate();

        Dish updated = dishDAO.update(dish);
        return DishMapper.toDTO(updated);
    }

    @Override
    public DishDTO activate(Long dishId)
    {
        ValidationUtil.validateId(dishId);

        Dish dish = dishDAO.getByID(dishId);
        dish.activate();

        Dish updated = dishDAO.update(dish);
        return DishMapper.toDTO(updated);
    }

    @Override
    public AvailableDishesDTO getAvailableDishesForMenu(int week, int year) {
        ValidationUtil.validateRange(week, 1, 53, "Week");
        ValidationUtil.validateRange(year, 2020, 2100, "Year");

        Set<Dish> newDishesFromThisWeek = dishDAO.findByOriginWeekAndYear(week, year);
        Set<Dish> fromDishHistory = dishDAO.findFromPreviousWeeks(week, year);

        Set<Long> allIds = new HashSet<>();
        newDishesFromThisWeek.forEach(d -> allIds.add(d.getId()));
        fromDishHistory.forEach(d -> allIds.add(d.getId()));
        Map<Long, String> lastServedMap = dishDAO.findLastServedBatch(allIds);

        return new AvailableDishesDTO(
            week,
            year,
            groupDishOptionsByStation(newDishesFromThisWeek, lastServedMap),
            groupDishOptionsByStation(fromDishHistory, lastServedMap)
        );
    }

    @Override
    public List<DishDTO> getAll(Long stationId, Boolean active)
    {
        if (stationId != null)
        {
            ValidationUtil.validateId(stationId);
        }

        return dishDAO.findByFilter(stationId, active)
            .stream()
            .map(DishMapper::toDTO)
            .toList();
    }

    @Override
    public Map<String, List<DishOptionDTO>> getAllActiveDishesGrouped() {
        Set<Dish> dishes = dishDAO.findByFilter(null, true);
        return groupDishOptionsByStationNoLastServed(dishes);
    }

    private Map<String, List<DishOptionDTO>> groupDishOptionsByStation(Set<Dish> dishes, Map<Long, String> lastServedMap) {
        return dishes.stream()
            .collect(Collectors.groupingBy(
                d -> d.getStation().getStationName(),
                Collectors.mapping(
                    d -> DishMapper.toOptionDTO(d, lastServedMap.get(d.getId())),
                    Collectors.toList()
                )
            ));
    }

    private Map<String, List<DishOptionDTO>> groupDishOptionsByStationNoLastServed(Set<Dish> dishes) {
        return dishes.stream()
            .collect(Collectors.groupingBy(
                d -> d.getStation().getStationName(),
                Collectors.mapping(
                    d -> DishMapper.toOptionDTO(d, null),
                    Collectors.toList()
                )
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
        ValidationUtil.validateId(dto.stationId());
        ValidationUtil.validateName(dto.nameDA(), "Name DA");
        ValidationUtil.validateDescription(dto.descriptionDA(), "Description DA");
    }

    private void validateUpdateInput(DishUpdateDTO dto) {
        ValidationUtil.validateNotNull(dto, "Dish update");
        ValidationUtil.validateName(dto.nameDA(), "Name DA");
        ValidationUtil.validateDescription(dto.descriptionDA(), "Description DA");

        if (dto.nameEN() != null)
        {
            ValidationUtil.validateName(dto.nameEN(), "Name EN");
        }

        if (dto.descriptionEN() != null)
        {
            ValidationUtil.validateDescription(dto.descriptionEN(), "Description EN");
        }
    }
}
