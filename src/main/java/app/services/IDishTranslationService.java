package app.services;

import app.dtos.dish.DishTranslationDTO;
import app.persistence.entities.Dish;

import java.util.List;
import java.util.Map;

public interface IDishTranslationService
{
    DishTranslationDTO translateDish(Dish dish, String targetLanguage);

    Map<Long, DishTranslationDTO> translateDishes(List<Dish> dishes, String targetLanguage);
}
