package app.config;

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
;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;

import java.net.http.HttpClient;


public class DIContainer
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ITranslationClient translationClient;
    private final IAiClient aiClient;
    private final WeatherClient weatherClient;
    private final IAiService aiService;
    private final IDishTranslationService dishTranslationService;

    private static IAllergenDAO allergenDAO;
    private static IDishDAO dishDAO;
    private static IDishSuggestionDAO dishSuggestionDAO;

    public DIContainer()
    {

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = ObjectMapperConfig.create();

        String deeplUrl = AppProperties.get("DEEPL_URL");
        String geminiUrl = AppProperties.get("GEMINI_URL");
        String openMeteoUrl = AppProperties.get("OPEN_METEO_URL");

        String deepLApiKey = System.getenv("DEEPL_APIKEY");
        String geminiApiKey = System.getenv("GEMINI_API_KEY");

        this.translationClient = new DeepLTranslationClient(httpClient, objectMapper, deeplUrl, deepLApiKey);
        this.aiClient = new GeminiClient(httpClient, objectMapper, geminiApiKey, geminiUrl);
        this.aiService = new AiService(objectMapper, aiClient);
        this.weatherClient = new WeatherClient(httpClient, objectMapper, openMeteoUrl);
        dishTranslationService = new DishTranslationService(translationClient);

    }

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
