package app.persistence.daos.interfaces.readers;

import app.persistence.daos.interfaces.generic.IEntityReader;
import app.persistence.entities.Dish;

public interface IDishReader extends IEntityReader<Dish, Long>
{
    boolean isUsedInAnyMenu(Long dishId);
}
