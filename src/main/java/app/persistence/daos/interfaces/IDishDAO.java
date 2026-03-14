package app.persistence.daos.interfaces;

import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.daos.interfaces.readers.IDishReader;
import app.persistence.entities.Dish;

import java.util.List;
import java.util.Set;

public interface IDishDAO extends IDishReader, IEntityDAO<Dish, Long>
{
    Set<Dish> findByOriginWeekAndYear(int week, int year);

    Set<Dish> findFromPreviousWeeks(int currentWeek, int currentYear);

    Set<Dish> searchByName(String query);

    Set<Dish> findByFilter(Long stationId, Boolean active);

    void updateAll(Set<Dish> dishes);
}
