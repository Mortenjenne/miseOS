package app.mappers;

import app.dtos.menu.WeeklyMenuDTO;
import app.dtos.menu.WeeklyMenuSlotDTO;
import app.persistence.entities.WeeklyMenu;
import app.persistence.entities.WeeklyMenuSlot;

import java.util.stream.Collectors;

public class WeeklyMenuMapper
{
    private WeeklyMenuMapper() {}

    public static WeeklyMenuDTO toDTO(WeeklyMenu menu)
    {
        return new WeeklyMenuDTO(
            menu.getId(),
            menu.getWeekNumber(),
            menu.getYear(),
            menu.getMenuStatus(),
            menu.getPublishedAt(),
            menu.getPublishedBy() != null ? menu.getPublishedBy().getFirstName() + " " + menu.getPublishedBy().getLastName() : null,
            menu.getWeeklyMenuSlots().stream()
                .map(WeeklyMenuMapper::toSlotDTO)
                .collect(Collectors.toSet())
        );
    }

    public static WeeklyMenuSlotDTO toSlotDTO(WeeklyMenuSlot slot)
    {
        return new WeeklyMenuSlotDTO(
            slot.getId(),
            slot.getDayOfWeek(),
            StationMapper.toDTO(slot.getStation()),
            slot.getDish() != null ? DishMapper.toDTO(slot.getDish()) : null
        );
    }
}
