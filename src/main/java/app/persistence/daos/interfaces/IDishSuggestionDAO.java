package app.persistence.daos.interfaces;

import app.enums.Status;
import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.daos.interfaces.readers.IDishSuggestionReader;
import app.persistence.entities.DishSuggestion;

import java.util.Set;

public interface IDishSuggestionDAO extends IDishSuggestionReader, IEntityDAO<DishSuggestion, Long>
{
    Set<DishSuggestion> findByFilter(Status status, Integer week, Integer year, Long stationId, String orderBy);
}
