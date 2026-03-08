package app.services.impl;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.exceptions.AIIntegrationException;
import app.integrations.ai.IAiClient;
import app.services.IAiService;
import app.utils.DishPromptBuilder;
import app.utils.NormalizeTextPromptBuilder;
import app.utils.WeatherForecastBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AiService implements IAiService
{
    private final ObjectMapper objectMapper;
    private final IAiClient aiClient;

    public AiService(ObjectMapper objectMapper, IAiClient aiClient)
    {
        this.objectMapper = objectMapper;
        this.aiClient = aiClient;
    }

    @Override
    public Map<String, String> normalizeIngredientList(List<String> ingredients, String targetLanguage)
    {
        String languageName = targetLanguage.equals("da") ? "Danish" : "English";
        try
        {
            String ingredientsJson = objectMapper.writeValueAsString(ingredients);
            String prompt = NormalizeTextPromptBuilder.buildNormalizeTextPrompt(ingredientsJson, languageName);
            String jsonResponse = aiClient.generateResponse(prompt);

            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        }
        catch (JsonProcessingException e)
        {
            throw new AIIntegrationException("Could not map jsonResponse to normalized text" + e.getMessage());
        }
        catch (Exception e)
        {
            throw new AIIntegrationException("Could not deserialize ingredients to json" + e.getMessage());
        }
    }

    @Override
    public List<AiDishSuggestionDTO> getAiDishSuggestion(WeatherForecastDTO weatherForecastDTO, StationDTO station)
    {
        try
        {
            String forecast = WeatherForecastBuilder.getWeatherForecast(weatherForecastDTO);
            String stationJSON = objectMapper.writeValueAsString(station);
            String prompt = DishPromptBuilder.buildMenuInspirationPrompt(forecast, stationJSON);

            String jsonResponse = aiClient.generateResponse(prompt);
            return Arrays.asList(objectMapper.readValue(jsonResponse, AiDishSuggestionDTO[].class));
        }
        catch (JsonProcessingException e)
        {
            throw new AIIntegrationException("Could not map jsonResponse to dish suggestion " + e.getMessage());
        }
    }
}
