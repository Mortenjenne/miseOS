package app.persistence.daos.interfaces.readers;

import app.persistence.daos.interfaces.generic.IEntityReader;
import app.persistence.entities.IngredientRequest;

public interface IIngredientRequestReader extends IEntityReader<IngredientRequest, Long>
{
    int getPendingRequestCount();
}
