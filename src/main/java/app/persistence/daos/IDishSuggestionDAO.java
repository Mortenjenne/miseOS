package app.persistence.daos;

import app.enums.Status;
import app.persistence.entities.DishSuggestion;

import java.util.Optional;
import java.util.Set;

public interface IDishSuggestionDAO extends IEntityDAO<DishSuggestion, Long>
{
    Set<DishSuggestion> findByStatus(Status status);
    Set<DishSuggestion> findByWeekAndYear(int weekNumber, int year);
    Set<DishSuggestion> findByStationAndStatus(Long stationId, Status status);
    Optional<DishSuggestion> getByIdWithAllergens(Long id);
}
