package app.services;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.persistence.entities.Station;

import java.util.List;
import java.util.Map;

public interface IAiService
{
    Map<String, String> normalizeIngredientList(List<String> ingredients, String targetLanguage);

    List<AiDishSuggestionDTO> getAiDishSuggestion(WeatherForecastDTO weatherForecast, StationDTO station);
}
