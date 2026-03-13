package app.persistence.daos.interfaces;

import app.enums.MenuStatus;
import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.entities.WeeklyMenu;

import java.util.Optional;
import java.util.Set;

public interface IWeeklyMenuDAO extends IEntityDAO<WeeklyMenu, Long>
{
    Set<WeeklyMenu> findByFilter(MenuStatus status, Integer year, Integer week);

    Optional<WeeklyMenu> findByWeekAndYear(int weekNumber, int year, MenuStatus status);
}
