package app.mappers;

import app.dtos.menu.WeeklyMenuDTO;
import app.dtos.menu.WeeklyMenuOverviewDTO;
import app.dtos.menu.WeeklyMenuSlotDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.WeeklyMenu;
import app.persistence.entities.WeeklyMenuSlot;

import java.util.Comparator;
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
            .sorted(Comparator
                .comparingInt((WeeklyMenuSlot slot) -> sortByDayOrder(slot.getDayOfWeek().name()))
                .thenComparingLong(slot -> slot.getStation().getId()))
            .map(WeeklyMenuMapper::toSlotDTO)
            .collect(Collectors.toList());

        return new WeeklyMenuDTO(
            menu.getId(),
            menu.getWeekNumber(),
            menu.getYear(),
            menu.getMenuStatus(),
            menu.getPublishedAt(),
            publishedBy,
            slots,
            slots.size()
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
            (long) weeklyMenu.getWeeklyMenuSlots().size(),
            weeklyMenu.getPublishedAt()
        );
    }

    private static int sortByDayOrder(String day) {
        return switch (day)
        {
            case "MONDAY" -> 1;
            case "TUESDAY" -> 2;
            case "WEDNESDAY" -> 3;
            case "THURSDAY" -> 4;
            case "FRIDAY" -> 5;
            default -> 99;
        };
    }
}
