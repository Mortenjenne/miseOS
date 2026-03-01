package app.persistence.daos.interfaces;

import app.persistence.entities.Dish;

import java.util.Optional;
import java.util.Set;

public interface IDishDAO extends IDishReader, IEntityDAO<Dish, Long>
{
    Set<Dish> findAllActive();
    Set<Dish> findByStationAndActive(Long stationId);
    Set<Dish> findByOriginWeekAndYear(int week, int year);
    Set<Dish> findFromPreviousWeeks(int currentWeek, int currentYear);
    Set<Dish> searchByName(String query);
}
