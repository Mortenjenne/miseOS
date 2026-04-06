package app.persistence.daos.interfaces;

import app.dtos.menu.WeeklyMenuOverviewDTO;
import app.enums.MenuStatus;
import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.daos.interfaces.readers.IWeeklyMenuReader;
import app.persistence.entities.WeeklyMenu;

import java.util.List;
import java.util.Optional;

public interface IWeeklyMenuDAO extends IWeeklyMenuReader, IEntityDAO<WeeklyMenu, Long>
{
    List<WeeklyMenuOverviewDTO> findByFilter(MenuStatus status, Integer year, Integer week);

    Optional<WeeklyMenu> findByWeekAndYear(int weekNumber, int year, MenuStatus status);
}
