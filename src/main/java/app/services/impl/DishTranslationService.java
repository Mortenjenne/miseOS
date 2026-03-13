package app.services.impl;

import app.dtos.dish.DishTranslationDTO;
import app.integrations.translation.ITranslationClient;
import app.persistence.entities.Dish;
import app.services.IDishTranslationService;
import app.utils.ValidationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DishTranslationService implements IDishTranslationService
{
    private final ITranslationClient translationClient;
    private static final int FIELDS_PER_DISH = 2;
    private static final int NAME_INDEX = 0;
    private static final int DESCRIPTION_INDEX = 1;

    public DishTranslationService(ITranslationClient translationClient)
    {
        this.translationClient = translationClient;
    }

    @Override
    public DishTranslationDTO translateDish(Dish dish, String targetLanguage) {
        validateDishInput(dish);
        ValidationUtil.validateNotBlank(targetLanguage, "Target language");

        List<String> results = translationClient.translateBatch(List.of(dish.getNameDA(), dish.getDescriptionDA()), targetLanguage);

        String nameEN = results.get(NAME_INDEX);
        String descriptionEN = results.get(DESCRIPTION_INDEX);

        return new DishTranslationDTO(nameEN, descriptionEN);
    }

    @Override
    public Map<Long, DishTranslationDTO> translateDishes(List<Dish> dishes, String targetLanguage)
    {
        ValidationUtil.validateNotNull(dishes, "Dishes");
        dishes.forEach(this::validateDishInput);
        ValidationUtil.validateNotBlank(targetLanguage, "Target language");

        if(dishes.isEmpty())
        {
            throw new IllegalArgumentException("Nothing to translate");
        }

        List<String> texts = dishes.stream()
            .flatMap(d -> Stream.of(d.getNameDA(), d.getDescriptionDA()))
            .toList();

        List<String> translations = translationClient.translateBatch(texts, targetLanguage);

        return buildDishTranslationMap(dishes, translations);
    }

    private Map<Long, DishTranslationDTO> buildDishTranslationMap(List<Dish> dishes, List<String> translations)
    {
        Map<Long, DishTranslationDTO> result = new HashMap<>();

        for (int i = 0; i < dishes.size(); i++)
        {
            Dish dish = dishes.get(i);
            int baseIndex = i * FIELDS_PER_DISH;

            String nameEN = translations.get(baseIndex + NAME_INDEX);
            String descriptionEN = translations.get(baseIndex + DESCRIPTION_INDEX);
            result.put(dish.getId(), new DishTranslationDTO(nameEN, descriptionEN));
        }
        return result;
    }

    private void validateDishInput(Dish dish)
    {
        ValidationUtil.validateNotNull(dish, "Dish");
        ValidationUtil.validateNotBlank(dish.getNameDA(), "Name DA");
        ValidationUtil.validateNotBlank(dish.getDescriptionDA(), "Description DA");
    }
}

