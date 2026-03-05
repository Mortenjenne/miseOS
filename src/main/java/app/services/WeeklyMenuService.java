package app.services;

import app.dtos.menu.AddMenuSlotDTO;
import app.dtos.menu.CreateWeeklyMenuDTO;
import app.dtos.menu.UpdateMenuSlotDTO;
import app.dtos.menu.WeeklyMenuDTO;
import app.enums.MenuStatus;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.mappers.WeeklyMenuMapper;
import app.persistence.daos.interfaces.*;
import app.persistence.entities.*;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WeeklyMenuService
{
    private final IWeeklyMenuDAO menuDAO;
    private final IDishReader dishReader;
    private final IUserReader userReader;
    private final IStationReader stationReader;
    private final IDishTranslationService dishTranslationService;


    public WeeklyMenuService(IWeeklyMenuDAO menuDAO, IDishReader dishReader, IUserReader userReader, IStationReader stationReader, IDishTranslationService dishTranslationService)
    {
        this.menuDAO = menuDAO;
        this.dishReader = dishReader;
        this.userReader = userReader;
        this.stationReader = stationReader;
        this.dishTranslationService = dishTranslationService;
    }

    public WeeklyMenuDTO createMenu(Long creatorId, CreateWeeklyMenuDTO dto)
    {
        ValidationUtil.validateId(creatorId);
        validateCreateInput(dto);

        User creator = userReader.getByID(creatorId);
        requireHeadOrSousChef(creator);

        Optional<WeeklyMenu> menu = menuDAO.findByWeekAndYear(dto.week(), dto.year());
        checkIfMenuExists(menu);

        WeeklyMenu weeklyMenu = new WeeklyMenu(dto.week(), dto.year());

        WeeklyMenu createdMenu = menuDAO.create(weeklyMenu);
        return WeeklyMenuMapper.toDTO(createdMenu);
    }

    public WeeklyMenuDTO addMenuSlot(Long editorId, Long menuId, AddMenuSlotDTO dto)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(menuId);
        validateSlotInput(dto);

        User editor = userReader.getByID(editorId);
        requireHeadOrSousChef(editor);

        WeeklyMenu menu = menuDAO.getByID(menuId);
        Station station = stationReader.getByID(dto.stationId());

        Dish dish = null;
        if(dto.dishId() != null)
        {
            dish = dishReader.getByID(dto.dishId());
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

    public WeeklyMenuDTO removeSlot(Long editorId, Long menuId, Long slotId)
    {
        validateSlotRelatedIds(editorId, menuId, slotId);

        User editor = userReader.getByID(editorId);
        requireHeadOrSousChef(editor);

        WeeklyMenu menu = menuDAO.getByIdWithSlots(menuId);

        WeeklyMenuSlot slot = findSlot(menu, slotId);
        menu.removeMenuSlot(slot);

        WeeklyMenu updated = menuDAO.update(menu);
        return WeeklyMenuMapper.toDTO(updated);
    }

    public WeeklyMenuDTO updateSlot(Long editorId, Long menuId, Long slotId, UpdateMenuSlotDTO dto)
    {
        validateSlotRelatedIds(editorId, menuId, slotId);

        User editor = userReader.getByID(editorId);
        requireHeadOrSousChef(editor);

        WeeklyMenu menu = menuDAO.getByIdWithSlots(menuId);
        WeeklyMenuSlot slot = findSlot(menu, slotId);

        if (dto.dishId() != null)
        {
            Dish dish = dishReader.getByID(dto.dishId());
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

    public WeeklyMenuDTO translateMenu(Long editorId, Long menuId)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(menuId);

        User editor = userReader.getByID(editorId);
        requireHeadOrSousChef(editor);

        WeeklyMenu menu = menuDAO.getByIdWithSlots(menuId);

        //TODO add stream of translations in to pass to translation service

        WeeklyMenu updated = menuDAO.getByIdWithSlots(menuId);
        return WeeklyMenuMapper.toDTO(updated);
    }

    public WeeklyMenuDTO publishMenu(Long publisherId, Long menuId)
    {
       ValidationUtil.validateId(publisherId);
       ValidationUtil.validateId(menuId);

       User publisher = userReader.getByID(publisherId);

       WeeklyMenu menu = menuDAO.getByIdWithSlots(menuId);
       requireNotEmpty(menu);
       validateAllDishesIsTranslated(menu);

       menu.publish(publisher);
       WeeklyMenu updated = menuDAO.update(menu);
       return WeeklyMenuMapper.toDTO(updated);
    }

    public WeeklyMenuDTO getByWeekAndYear(int week, int year)
    {
        validateWeekAndYear(week, year);

        return menuDAO.findByWeekAndYear(week, year)
            .map(WeeklyMenuMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("No menu for week " + week + "/" + year)
            );
    }

    public WeeklyMenuDTO getCurrentWeekMenu()
    {
        LocalDate today = LocalDate.now();
        int week = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = today.get(IsoFields.WEEK_BASED_YEAR);

        return menuDAO.findByWeekAndYear(week, year)
            .map(WeeklyMenuMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("No menu published for current week " + week + "/" + year)
            );
    }

    public WeeklyMenuDTO getById(Long id)
    {
        ValidationUtil.validateId(id);
        WeeklyMenu menu = menuDAO.getByIdWithSlots(id);
        return WeeklyMenuMapper.toDTO(menu);
    }

    public Set<WeeklyMenuDTO> getAllMenus()
    {
        return menuDAO.getAll()
            .stream()
            .map(WeeklyMenuMapper::toDTO)
            .collect(Collectors.toSet());
    }

    public Set<WeeklyMenuDTO> getPublishedMenusByStatus(MenuStatus status)
    {
        ValidationUtil.validateNotNull(status, "Menu status");

        return menuDAO.findByStatus(status)
            .stream()
            .map(WeeklyMenuMapper::toDTO)
            .collect(Collectors.toSet());
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

    private void checkIfMenuExists(Optional<WeeklyMenu> menu)
    {
        if (menu.isPresent())
        {
            throw new IllegalStateException("Menu for week " + menu.get().getWeekNumber() + "/" + menu.get().getYear() + " already exists");
        }
    }

    private void requireHeadOrSousChef(User user)
    {
        if (!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef and sous chef can manage menus");
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

    private void validateSlotInput(AddMenuSlotDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Slot");
        ValidationUtil.validateNotNull(dto.dayOfWeek(), "Day of week");
        ValidationUtil.validateId(dto.stationId());
    }

    private void validateSlotRelatedIds(Long editorId, Long menuId, Long slotId)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(menuId);
        ValidationUtil.validateId(slotId);
    }
}
