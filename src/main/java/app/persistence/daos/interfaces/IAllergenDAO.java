package app.persistence.daos.interfaces;

import app.persistence.entities.Allergen;

import java.util.Optional;

public interface IAllergenDAO extends IEntityDAO<Allergen, Long>
{
    Optional<Allergen> findByName(String name);
}
