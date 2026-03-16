package app.services;

import app.dtos.menu.*;
import app.enums.MenuStatus;
import app.enums.SupportedLanguage;

import java.util.List;

public interface IWeeklyMenuService
{
    WeeklyMenuDTO createMenu(Long creatorId, CreateWeeklyMenuDTO dto);

    WeeklyMenuDTO addMenuSlot(Long editorId, Long menuId, AddMenuSlotDTO dto);

    WeeklyMenuDTO removeSlot(Long editorId, Long menuId, Long slotId);

    WeeklyMenuDTO updateSlot(Long editorId, Long menuId, Long slotId, UpdateMenuSlotDTO dto);

    WeeklyMenuDTO translateSlot(Long userId, Long menuId, Long slotId, SupportedLanguage language);

    WeeklyMenuDTO translateMenu(Long editorId, Long menuId, SupportedLanguage language);

    WeeklyMenuDTO publishMenu(Long publisherId, Long menuId);

    WeeklyMenuDTO getByWeekAndYear(Long userId, int week, int year);

    WeeklyMenuDTO getCurrentWeekMenu();

    WeeklyMenuDTO getById(Long id);

    List<WeeklyMenuOverviewDTO> getOverview(Long userId, MenuStatus menuStatus, Integer year, Integer week);

    boolean deleteMenu(Long userId, Long menuId);
}
