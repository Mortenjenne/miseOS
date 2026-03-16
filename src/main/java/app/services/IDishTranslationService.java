package app.services;

import app.dtos.dish.DishTranslationDTO;
import app.persistence.entities.Dish;

import java.util.Map;
import java.util.Set;

public interface IDishTranslationService
{
    DishTranslationDTO translateDish(Dish dish, String targetLanguage);

    Map<Long, DishTranslationDTO> translateDishes(Set<Dish> dishes, String targetLanguage);
}
