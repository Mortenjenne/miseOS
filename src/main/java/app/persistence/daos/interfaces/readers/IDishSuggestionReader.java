package app.persistence.daos.interfaces.readers;

import app.persistence.daos.interfaces.generic.IEntityReader;
import app.persistence.entities.DishSuggestion;

public interface IDishSuggestionReader extends IEntityReader<DishSuggestion, Long>
{
    int getPendingSuggestionsCount();
}
