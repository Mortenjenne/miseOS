package app.services;

import app.dtos.dishsuggestion.DishTranslationDTO;
import app.persistence.entities.DishSuggestion;

public interface IDishTranslationService
{
    DishTranslationDTO translateTo(DishSuggestion dish, String targetLanguage);
}
