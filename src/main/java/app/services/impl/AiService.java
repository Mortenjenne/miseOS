package app.services.impl;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.exceptions.AIIntegrationException;
import app.integrations.ai.IAiClient;
import app.persistence.entities.Station;
import app.services.IAiService;
import app.utils.DishPromptBuilder;
import app.utils.NormalizeTextPromptBuilder;
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
    public List<AiDishSuggestionDTO> getAiDishSuggestion(WeatherForecastDTO weatherForecast, StationDTO station)
    {
        try
        {
            String weatherJSON = objectMapper.writeValueAsString(weatherForecast);
            String stationJSON = objectMapper.writeValueAsString(station);
            String prompt = DishPromptBuilder.buildMenuInspirationPrompt(weatherJSON, stationJSON);
            System.out.println(prompt);

            String jsonResponse = aiClient.generateResponse(prompt);
            System.out.println(jsonResponse);

            return Arrays.asList(objectMapper.readValue(jsonResponse, AiDishSuggestionDTO[].class));
        }
        catch (JsonProcessingException e)
        {
            throw new AIIntegrationException("Could not map jsonResponse to dish suggestion " + e.getMessage());
        }
    }
}
