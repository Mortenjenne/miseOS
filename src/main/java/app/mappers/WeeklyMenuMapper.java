package app.mappers;

import app.dtos.menu.WeeklyMenuDTO;
import app.dtos.menu.WeeklyMenuOverviewDTO;
import app.dtos.menu.WeeklyMenuSlotDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.WeeklyMenu;
import app.persistence.entities.WeeklyMenuSlot;

import java.util.List;
import java.util.stream.Collectors;

public class WeeklyMenuMapper
{
    private WeeklyMenuMapper() {}

    public static WeeklyMenuDTO toDTO(WeeklyMenu menu)
    {
        UserReferenceDTO publishedBy = UserMapper.toReferenceDTO(menu.getPublishedBy());

        List<WeeklyMenuSlotDTO> slots = menu.getWeeklyMenuSlots()
            .stream()
            .map(WeeklyMenuMapper::toSlotDTO)
            .toList();

        return new WeeklyMenuDTO(
            menu.getId(),
            menu.getWeekNumber(),
            menu.getYear(),
            menu.getMenuStatus(),
            menu.getPublishedAt(),
            publishedBy,
            slots
        );
    }

    public static WeeklyMenuSlotDTO toSlotDTO(WeeklyMenuSlot slot)
    {
        if(slot == null) return null;

        return new WeeklyMenuSlotDTO(
            slot.getId(),
            slot.getDayOfWeek(),
            StationMapper.toReferenceDTO(slot.getStation()),
            DishMapper.toDishMenuDTO(slot.getDish())
        );
    }

    public static WeeklyMenuOverviewDTO toOverviewDTO(WeeklyMenu weeklyMenu)
    {
        return new WeeklyMenuOverviewDTO(
            weeklyMenu.getId(),
            weeklyMenu.getWeekNumber(),
            weeklyMenu.getYear(),
            weeklyMenu.getMenuStatus(),

            weeklyMenu.getPublishedAt()
        );
    }
}
