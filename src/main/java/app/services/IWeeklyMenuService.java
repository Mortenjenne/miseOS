package app.services;

import app.dtos.menu.*;
import app.enums.MenuStatus;

import java.util.List;

public interface IWeeklyMenuService
{
    WeeklyMenuDTO createMenu(Long creatorId, CreateWeeklyMenuDTO dto);

    WeeklyMenuDTO addMenuSlot(Long editorId, Long menuId, AddMenuSlotDTO dto);

    WeeklyMenuDTO removeSlot(Long editorId, Long menuId, Long slotId);

    WeeklyMenuDTO updateSlot(Long editorId, Long menuId, Long slotId, UpdateMenuSlotDTO dto);

    WeeklyMenuDTO translateMenu(Long editorId, Long menuId);

    WeeklyMenuDTO publishMenu(Long publisherId, Long menuId);

    WeeklyMenuDTO getByWeekAndYear(Long userId, int week, int year);

    WeeklyMenuDTO getCurrentWeekMenu();

    WeeklyMenuDTO getById(Long id);

    List<WeeklyMenuOverviewDTO> getOverview(Long userId, MenuStatus menuStatus, Integer year, Integer week);

    boolean deleteMenu(Long userId, Long menuId);
}
