package app.services.impl;

import app.dtos.dishsuggestion.DishTranslationDTO;
import app.integrations.translation.ITranslationClient;
import app.persistence.entities.DishSuggestion;
import app.services.IDishTranslationService;

public class DishTranslationService implements IDishTranslationService
{
    private final ITranslationClient translationService;

    public DishTranslationService(ITranslationClient translationService)
    {
        this.translationService = translationService;
    }

    @Override
    public DishTranslationDTO translateTo(DishSuggestion dish, String targetLanguage) {

        if (dish == null)
        {
            throw new IllegalArgumentException("Dish cannot be null");
        }

        String translatedName = translationService.translate(dish.getNameDA(), targetLanguage);
        String translatedDescription = translationService.translate(dish.getDescriptionDA(), targetLanguage);

        return new DishTranslationDTO(translatedName, translatedDescription);
    }
}

