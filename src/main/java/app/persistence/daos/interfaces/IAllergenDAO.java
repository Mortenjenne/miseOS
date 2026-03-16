package app.persistence.daos.interfaces;

import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.daos.interfaces.readers.IAllergenReader;
import app.persistence.entities.Allergen;

import java.util.Optional;
import java.util.Set;

public interface IAllergenDAO extends IEntityDAO<Allergen, Long>, IAllergenReader
{
    Optional<Allergen> findByNameDA(String name);

    Optional<Allergen> findByNameEN(String nameEN);

    Set<Allergen> getAll();

    long count();
}
