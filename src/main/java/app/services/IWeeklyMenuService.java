package app.services;

import app.dtos.menu.AddMenuSlotDTO;
import app.dtos.menu.CreateWeeklyMenuDTO;
import app.dtos.menu.UpdateMenuSlotDTO;
import app.dtos.menu.WeeklyMenuDTO;
import app.enums.MenuStatus;

import java.util.Set;

public interface IWeeklyMenuService
{
    WeeklyMenuDTO createMenu(Long creatorId, CreateWeeklyMenuDTO dto);

    WeeklyMenuDTO addMenuSlot(Long editorId, Long menuId, AddMenuSlotDTO dto);

    WeeklyMenuDTO removeSlot(Long editorId, Long menuId, Long slotId);

    WeeklyMenuDTO updateSlot(Long editorId, Long menuId, Long slotId, UpdateMenuSlotDTO dto);

    WeeklyMenuDTO translateMenu(Long editorId, Long menuId);

    WeeklyMenuDTO publishMenu(Long publisherId, Long menuId);

    WeeklyMenuDTO getByWeekAndYear(int week, int year);

    WeeklyMenuDTO getCurrentWeekMenu();

    WeeklyMenuDTO getById(Long id);

    Set<WeeklyMenuDTO> getAllMenus();

    Set<WeeklyMenuDTO> getPublishedMenusByStatus(MenuStatus status);
}
