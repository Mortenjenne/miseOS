package app.persistence.daos;

import app.enums.MenuStatus;
import app.persistence.entities.WeeklyMenu;

import java.util.Optional;
import java.util.Set;

public interface IWeeklyMenuDAO extends IEntityDAO<WeeklyMenu, Long>
{
    Set<WeeklyMenu> findByStatus(MenuStatus status);
    Optional<WeeklyMenu> findByWeekAndYear(int weekNumber, int year);
    WeeklyMenu getByIdWithSlots(Long id);

}
