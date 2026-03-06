package app.persistence.daos.interfaces;

import app.persistence.entities.Dish;

import java.util.Optional;

public interface IDishReader extends IEntityReader<Dish, Long>
{
    Optional<Dish> getByIdWithAllergens(Long id);
    boolean isUsedInAnyMenu(Long dishId);
}
