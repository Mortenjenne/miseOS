package app.persistence.daos.interfaces;

import app.persistence.entities.DishSuggestion;

import java.util.Optional;

public interface IDishSuggestionReader extends IEntityReader<DishSuggestion, Long>
{
    Optional<DishSuggestion> getByIdWithAllergens(Long id);
}
