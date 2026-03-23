package app.services;

import app.dtos.menu.*;
import app.dtos.security.AuthenticatedUser;
import app.enums.MenuStatus;
import app.enums.SupportedLanguage;

import java.util.List;

public interface IWeeklyMenuService
{
    WeeklyMenuDTO createMenu(CreateWeeklyMenuDTO dto);

    WeeklyMenuDTO addMenuSlot(Long menuId, AddMenuSlotDTO dto);

    WeeklyMenuDTO removeSlot(Long menuId, Long slotId);

    WeeklyMenuDTO updateSlot(Long menuId, Long slotId, UpdateMenuSlotDTO dto);

    WeeklyMenuDTO translateSlot(Long menuId, Long slotId, SupportedLanguage language);

    WeeklyMenuDTO translateMenu(Long menuId, SupportedLanguage language);

    WeeklyMenuDTO publishMenu(AuthenticatedUser authUser, Long menuId);

    WeeklyMenuDTO getByWeekAndYear(AuthenticatedUser authUser, int week, int year);

    WeeklyMenuDTO getCurrentWeekMenu();

    WeeklyMenuDTO getById(Long menuId);

    List<WeeklyMenuOverviewDTO> getOverview(MenuStatus menuStatus, Integer year, Integer week);

    boolean deleteMenu(AuthenticatedUser authUser, Long menuId);
}
