package app.config;

import app.dtos.dishsuggestion.DishTranslationDTO;
import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.enums.UserRole;
import app.integrations.ai.GeminiClient;
import app.integrations.ai.IAiClient;
import app.integrations.translation.DeepLTranslationClient;
import app.integrations.translation.ITranslationClient;
import app.integrations.weather.WeatherClient;
import app.persistence.entities.Allergen;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.services.*;
import app.utils.WeatherForecastBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.*;

public class ApplicationConfig
{
    public static void start()
    {
        Properties properties = new Properties();

        try (InputStream input = ApplicationConfig.class
                     .getClassLoader()
                     .getResourceAsStream("config.properties")) {

            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String deeplUrl = properties.getProperty("DEEPL_URL");
        String geminiUrl = properties.getProperty("GEMINI_URL");
        String openMeteoUrl = properties.getProperty("OPEN_METEO_URL");
        String deepLApiKey = System.getenv("DEEPL_APIKEY");
        String geminiApiKey = System.getenv("GEMINI_API_KEY");

        ITranslationClient translationService = new DeepLTranslationClient(client, objectMapper, deeplUrl, deepLApiKey);
        IAiClient aiClient = new GeminiClient(client, objectMapper, geminiApiKey, geminiUrl);
        IAiService aiService = new AiService(objectMapper, aiClient);
        WeatherClient weatherClient = new WeatherClient(client, objectMapper, openMeteoUrl);


        IDishTranslationService dishTranslationService = new DishTranslationService(translationService);
        WeatherForecastDTO weatherForecastDTO = weatherClient.getWeatherForecast();
        String forecast = WeatherForecastBuilder.getWeatherForecast(weatherForecastDTO);

        List<AiDishSuggestionDTO> dishSuggestionDTOs = aiService.getAiDishSuggestion(forecast, "Sandwich station");
        dishSuggestionDTOs.forEach(System.out::println);

        List<String> ingredientsToNormalize = List.of(
            "onions",
            "løg",
            "red onion",
            "rødløg",
            "hvidløg",
            "garlic",
            "hvidløch",
            "potatoes",
            "nye kartofler",
            "carots",
            "gulerødder",
            "gulerod",
            "piskefløde",
            "heavy cream",
            "cream",
            "smør",
            "unsalted butter",
            "butter",
            "salt",
            "havsalt",
            "pepper",
            "sort peber",
            "peber",
            "shallots",
            "skalotteløg",
            "skalotte løg",
            "spring onions",
            "forårsløg",
            "chives",
            "purløg",
            "parsley",
            "persille",
            "lemon",
            "citron",
            "lime juice",
            "limes",
            "olive oil",
            "olivenolie",
            "extra virgin olive oil"
        );



        Map<String, String> result = aiService.normalizeIngredientList(ingredientsToNormalize, "da");

        result.forEach((k, v) -> System.out.println("Key: " + k + " Value " + v));


    }
}
