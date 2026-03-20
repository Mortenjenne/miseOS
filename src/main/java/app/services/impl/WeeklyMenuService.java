package app.services.impl;

import app.dtos.dish.DishTranslationDTO;
import app.dtos.menu.*;
import app.dtos.security.AuthenticatedUser;
import app.enums.MenuStatus;
import app.enums.Role;
import app.enums.SupportedLanguage;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.mappers.WeeklyMenuMapper;
import app.persistence.daos.interfaces.*;
import app.persistence.daos.interfaces.readers.IStationReader;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.*;
import app.services.IDishTranslationService;
import app.services.IWeeklyMenuService;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;


public class WeeklyMenuService implements IWeeklyMenuService
{
    private final IWeeklyMenuDAO menuDAO;
    private final IDishDAO dishDAO;
    private final IUserReader userReader;
    private final IStationReader stationReader;
    private final IDishTranslationService dishTranslationService;

    public WeeklyMenuService(IWeeklyMenuDAO menuDAO, IDishDAO dishDAO, IUserReader userReader, IStationReader stationReader, IDishTranslationService dishTranslationService)
    {
        this.menuDAO = menuDAO;
        this.dishDAO = dishDAO;
        this.userReader = userReader;
        this.stationReader = stationReader;
        this.dishTranslationService = dishTranslationService;
    }

    @Override
    public WeeklyMenuDTO createMenu(CreateWeeklyMenuDTO dto)
    {
        validateCreateInput(dto);

        Optional<WeeklyMenu> existingMenu = menuDAO.findByWeekAndYear(dto.week(), dto.year(), null);
        if (existingMenu.isPresent())
        {
            throw new IllegalStateException("Menu for week " + dto.week() + "/" + dto.year() + " already exists");
        }

        WeeklyMenu weeklyMenu = new WeeklyMenu(dto.week(), dto.year());

        WeeklyMenu createdMenu = menuDAO.create(weeklyMenu);
        return WeeklyMenuMapper.toDTO(createdMenu);
    }

    @Override
    public WeeklyMenuDTO addMenuSlot(Long menuId, AddMenuSlotDTO dto)
    {
        ValidationUtil.validateId(menuId);
        validateSlotInput(dto);

        WeeklyMenu menu = menuDAO.getByID(menuId);
        Station station = stationReader.getByID(dto.stationId());

        Dish dish = null;
        if(dto.dishId() != null)
        {
            dish = dishDAO.getByID(dto.dishId());
            validateDishForStation(dish, station);
        }

        WeeklyMenuSlot menuSlot = new WeeklyMenuSlot(
            dto.dayOfWeek(),
            dish,
            station
        );

        menu.addMenuSlot(menuSlot);
        WeeklyMenu updated = menuDAO.update(menu);
        return WeeklyMenuMapper.toDTO(updated);
    }

    @Override
    public WeeklyMenuDTO removeSlot(Long menuId, Long slotId)
    {
        validateSlotRelatedIds(menuId, slotId);

        WeeklyMenu menu = menuDAO.getByID(menuId);
        WeeklyMenuSlot slot = findSlot(menu, slotId);
        menu.removeMenuSlot(slot);

        WeeklyMenu updated = menuDAO.update(menu);
        return WeeklyMenuMapper.toDTO(updated);
    }

    @Override
    public WeeklyMenuDTO updateSlot(Long menuId, Long slotId, UpdateMenuSlotDTO dto)
    {
        validateSlotRelatedIds(menuId, slotId);

        WeeklyMenu menu = menuDAO.getByID(menuId);
        WeeklyMenuSlot slot = findSlot(menu, slotId);

        if (dto.dishId() != null)
        {
            Dish dish = dishDAO.getByID(dto.dishId());
            validateDishForStation(dish, slot.getStation());
            slot.setDish(dish);
            slot.setEmpty(false);
        }
        else
        {
            slot.setDish(null);
            slot.setEmpty(true);
        }

        WeeklyMenu updated = menuDAO.update(menu);
        return WeeklyMenuMapper.toDTO(updated);
    }

    @Override
    public WeeklyMenuDTO translateSlot(Long menuId, Long slotId, SupportedLanguage language)
    {
        validateSlotRelatedIds(menuId, slotId);
        ValidationUtil.validateNotNull(language, "Target language");

        WeeklyMenu menu = menuDAO.getByID(menuId);
        WeeklyMenuSlot slot = findSlot(menu, slotId);

        Dish dish = slot.getDish();

        if (dish == null)
        {
            throw new IllegalStateException("Slot " + slotId + " has no dish to translate");
        }

        DishTranslationDTO translation = dishTranslationService.translateDish(dish, language.getCode());
        dish.applyTranslation(
            translation.translatedName(),
            translation.translatedDescription()
        );
        dishDAO.update(dish);

        WeeklyMenu updated = menuDAO.getByID(menuId);
        return WeeklyMenuMapper.toDTO(updated);
    }

    @Override
    public WeeklyMenuDTO translateMenu(Long menuId, SupportedLanguage language)
    {
        ValidationUtil.validateId(menuId);
        ValidationUtil.validateNotNull(language, "Target language");

        WeeklyMenu menu = menuDAO.getByID(menuId);
        Set<Dish> dishes = getDishesFromMenu(menu);

        Map<Long, DishTranslationDTO> dishTranslations = dishTranslationService.translateDishes(dishes, language.getCode());
        updateDishesWithTranslations(dishes, dishTranslations);

        WeeklyMenu updated = menuDAO.getByID(menuId);
        return WeeklyMenuMapper.toDTO(updated);
    }

    @Override

    public WeeklyMenuDTO publishMenu(AuthenticatedUser authUser, Long menuId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(menuId);

        User publisher = userReader.getByID(authUser.userId());

        WeeklyMenu menu = menuDAO.getByID(menuId);
        requireNotEmpty(menu);
        validateAllDishesIsTranslated(menu);

        menu.publish(publisher);
        WeeklyMenu updated = menuDAO.update(menu);
        return WeeklyMenuMapper.toDTO(updated);
    }

    @Override
    public WeeklyMenuDTO getByWeekAndYear(AuthenticatedUser authUser, int week, int year)
    {
        validateAuthenticatedUser(authUser);
        validateWeekAndYear(week, year);

        User user = userReader.getByID(authUser.userId());
        MenuStatus menuStatus = getMenuStatusPermission(user);

        return menuDAO.findByWeekAndYear(week, year, menuStatus)
            .map(WeeklyMenuMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("No menu for week " + week + "/" + year)
            );
    }

    @Override
    public WeeklyMenuDTO getCurrentWeekMenu()
    {
        LocalDate today = LocalDate.now();
        int week = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = today.get(IsoFields.WEEK_BASED_YEAR);

        return menuDAO.findByWeekAndYear(week, year, MenuStatus.PUBLISHED)
            .map(WeeklyMenuMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("No menu published for current week " + week + "/" + year));
    }

    @Override
    public WeeklyMenuDTO getById(Long id)
    {
        ValidationUtil.validateId(id);
        WeeklyMenu menu = menuDAO.getByID(id);
        return WeeklyMenuMapper.toDTO(menu);
    }

    @Override
    public List<WeeklyMenuOverviewDTO> getOverview(MenuStatus menuStatus, Integer year, Integer week)
    {
        return menuDAO.findByFilter(menuStatus, year, week);
    }

    @Override
    public boolean deleteMenu(AuthenticatedUser authUser, Long menuId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(menuId);

        User user = userReader.getByID(authUser.userId());
        WeeklyMenu menu = menuDAO.getByID(menuId);

        menu.delete(user);
        return menuDAO.delete(menuId);
    }

    private WeeklyMenuSlot findSlot(WeeklyMenu menu, Long menuSlotId)
    {
        return menu.getWeeklyMenuSlots()
            .stream()
            .filter(m -> m.getId().equals(menuSlotId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Slot " + menuSlotId + " not found in menu " + menu.getId()
            ));
    }

    private static Set<Dish> getDishesFromMenu(WeeklyMenu menu)
    {
        return menu.getWeeklyMenuSlots()
            .stream()
            .map(WeeklyMenuSlot::getDish)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private void updateDishesWithTranslations(Set<Dish> dishes, Map<Long, DishTranslationDTO> dishTranslations)
    {
        dishes.forEach(dish ->
        {
            DishTranslationDTO translation = dishTranslations.get(dish.getId());
            dish.applyTranslation(
                translation.translatedName(),
                translation.translatedDescription()
            );
        });
        dishDAO.updateAll(dishes);
    }

    private void validateWeekAndYear(int week, int year)
    {
        ValidationUtil.validateRange(week, 1, 53, "Week");
        ValidationUtil.validateRange(year, 2020, 2100, "Year");
    }

    private void validateDishForStation(Dish dish, Station station)
    {
        if (!dish.isActive())
        {
            throw new IllegalStateException("Dish is not active");
        }

        if (!dish.getStation().getId().equals(station.getId()))
        {
            throw new ValidationException("Dish belongs to '" + dish.getStation().getStationName() + "' not '" + station.getStationName() + "'");
        }
    }

    private void requireNotEmpty(WeeklyMenu menu)
    {
        if (menu.getWeeklyMenuSlots().isEmpty())
        {
            throw new IllegalStateException("Cannot publish an empty menu");
        }
    }
    
    private void validateAllDishesIsTranslated(WeeklyMenu menu)
    {
        Set<String> untranslatedDishes = menu.getWeeklyMenuSlots()
            .stream()
            .filter(slot -> slot.getDish() != null && !slot.getDish().hasTranslation())
            .map(dish -> dish.getDish().getNameDA())
            .collect(Collectors.toSet()
            );

        if(!untranslatedDishes.isEmpty())
        {
            throw new IllegalStateException("Cannot publish - untranslated dishes: " + String.join(", ", untranslatedDishes));
        }
    }

    private void validateCreateInput(CreateWeeklyMenuDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Weekly Menu");
        validateWeekAndYear(dto.week(), dto.year());
    }

    private void validateAuthenticatedUser(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
    }

    private void validateSlotInput(AddMenuSlotDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Slot");
        ValidationUtil.validateNotNull(dto.dayOfWeek(), "Day of week");
        ValidationUtil.validateId(dto.stationId());
    }

    private void validateSlotRelatedIds(Long menuId, Long slotId)
    {
        ValidationUtil.validateId(menuId);
        ValidationUtil.validateId(slotId);
    }

    private MenuStatus getMenuStatusPermission(User user)
    {
        if (!user.isKitchenStaff())
        {
            return MenuStatus.PUBLISHED;
        }
        return null;
    }
}
