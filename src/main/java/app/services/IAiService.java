package app.services;

import app.dtos.gemini.AiDishSuggestionDTO;

import java.util.List;
import java.util.Map;

public interface IAiService
{
    Map<String, String> normalizeIngredientList(List<String> ingredients, String targetLanguage);

    List<AiDishSuggestionDTO> getAiDishSuggestion(String weatherForecast, String stationName);
}
