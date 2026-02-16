package app.persistence.daos;

import app.persistence.entities.Allergen;

import java.util.List;
import java.util.Optional;

public interface IAllergenDAO extends IEntityDAO<Allergen, Long>
{
    Optional<Allergen> findByName(String name);
}
