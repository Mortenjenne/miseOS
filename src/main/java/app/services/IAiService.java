package app.services;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.enums.SupportedLanguage;
import app.persistence.entities.Station;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface IAiService
{
    Map<String, String> normalizeIngredientList(List<String> ingredients, SupportedLanguage targetLanguage);

    List<AiDishSuggestionDTO> getAiDishSuggestion(WeatherForecastDTO weatherForecast, StationDTO station);

    void getStreamingDishSuggestions(WeatherForecastDTO weatherForecastDTO, StationDTO station, Consumer<AiDishSuggestionDTO> dishConsumer, Runnable onComplete, Consumer<Throwable> errorConsumer);
}
