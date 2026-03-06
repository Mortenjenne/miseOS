package app.config;

import app.dtos.weather.WeatherForecastDTO;
import app.integrations.ai.GeminiClient;
import app.integrations.ai.IAiClient;
import app.integrations.translation.DeepLTranslationClient;
import app.integrations.translation.ITranslationClient;
import app.integrations.weather.WeatherClient;
import app.persistence.daos.impl.AllergenDAO;
import app.persistence.daos.impl.DishDAO;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.daos.interfaces.IDishDAO;
import app.persistence.daos.interfaces.IDishSuggestionDAO;
import app.services.IAiService;
import app.services.IDishTranslationService;
import app.services.impl.AiService;
import app.services.impl.DishTranslationService;
import app.utils.WeatherForecastBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.Properties;

public class DIContainer
{
    Properties properties = new Properties();

        try (
    InputStream input = ApplicationConfig.class
        .getClassLoader()
        .getResourceAsStream("config.properties")) {

    properties.load(input);

} catch (
    IOException e) {
    throw new RuntimeException(e);
}

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    private static IAllergenDAO allergenDAO;
    private static IDishDAO dishDAO;
    private static IDishSuggestionDAO dishSuggestionDAO;



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



    private static synchronized IAllergenDAO getAllergenDAO()
    {
        if(allergenDAO == null)
        {
            allergenDAO = new AllergenDAO(emf);
        }
        return allergenDAO;
    }

    private static synchronized IDishDAO getDishDAO()
    {
        if(dishDAO == null)
        {
            dishDAO = new DishDAO(emf);
        }
        return dishDAO;
    }
}
