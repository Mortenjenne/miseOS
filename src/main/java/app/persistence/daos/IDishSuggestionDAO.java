package app.persistence.daos;

import app.enums.Status;
import app.persistence.entities.DishSuggestion;

import java.util.Optional;
import java.util.Set;

public interface IDishSuggestionDAO extends IEntityDAO<DishSuggestion, Long>
{
    Set<DishSuggestion> findByStatus(Status status);
    Set<DishSuggestion> findByWeekYearAndStatus(int week, int year, Status status);
    Set<DishSuggestion> findByStationAndStatus(Long stationId, Status status);
    Optional<DishSuggestion> getByIdWithAllergens(Long id);
}
