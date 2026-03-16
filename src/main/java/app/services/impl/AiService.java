package app.services.impl;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.enums.SupportedLanguage;
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
import java.util.function.Consumer;

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
    public Map<String, String> normalizeIngredientList(List<String> ingredients, SupportedLanguage targetLanguage)
    {
        String languageName = targetLanguage.getDisplayName();
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

    @Override
    public void getStreamingDishSuggestions(WeatherForecastDTO weatherForecastDTO, StationDTO station, Consumer<AiDishSuggestionDTO> dishConsumer, Runnable onComplete, Consumer<Throwable> errorConsumer)
    {
        StringBuilder fullResponse = new StringBuilder();

        try
        {
            String forecast = WeatherForecastBuilder.getWeatherForecast(weatherForecastDTO);
            String stationJSON = objectMapper.writeValueAsString(station);
            String prompt = DishPromptBuilder.buildMenuInspirationPrompt(forecast, stationJSON);

            aiClient.streamResponse(
                prompt,
                fullResponse::append,
                errorConsumer,
                () ->
                {
                    try
                    {
                        String json = cleanResponseBody(fullResponse.toString());
                        AiDishSuggestionDTO[] dishes = objectMapper.readValue(json, AiDishSuggestionDTO[].class);
                        for (AiDishSuggestionDTO dish : dishes)
                        {
                            dishConsumer.accept(dish);
                        }
                        onComplete.run();
                    }
                    catch (Exception e)
                    {
                        errorConsumer.accept(new AIIntegrationException("Could not parse streaming result"));
                    }
                });
        }
        catch (Exception e)
        {
            errorConsumer.accept(e);
        }

    }
    private String cleanResponseBody(String response)
    {
        return response
            .replace("```json", "")
            .replace("```", "")
            .trim();
    }
}
