package app.persistence.daos.interfaces;

import app.enums.Status;
import app.persistence.entities.DishSuggestion;

import java.util.Set;

public interface IDishSuggestionDAO extends IDishSuggestionReader, IEntityDAO<DishSuggestion, Long>
{
    Set<DishSuggestion> findByStatus(Status status);
    Set<DishSuggestion> findByWeekYearAndStatus(int week, int year, Status status);
    Set<DishSuggestion> findByStationAndStatus(Long stationId, Status status);
}
