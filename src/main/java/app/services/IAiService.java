package app.services;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.menu.RecentMenuDishDTO;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.enums.SupportedLanguage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface IAiService
{
    Map<String, String> normalizeIngredientList(List<String> ingredients, SupportedLanguage targetLanguage);

    List<AiDishSuggestionDTO> getAiDishSuggestion(WeatherForecastDTO weatherForecast, StationDTO station, List<RecentMenuDishDTO> recentMenuDishDTOS);

    void getStreamingDishSuggestions(WeatherForecastDTO weatherForecastDTO, StationDTO station, List<RecentMenuDishDTO> recentMenuDishDTOS, Consumer<AiDishSuggestionDTO> dishConsumer, Runnable onComplete, Consumer<Throwable> errorConsumer);
}
