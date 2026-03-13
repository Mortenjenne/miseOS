package app.persistence.daos.interfaces.readers;

import app.persistence.daos.interfaces.generic.IEntityReader;
import app.persistence.entities.Allergen;

public interface IAllergenReader extends IEntityReader<Allergen, Long>
{
    boolean isUsedByAnyDish(Long allergenId);

    boolean existsByDisplayNumber(Integer displayNumber);
}
