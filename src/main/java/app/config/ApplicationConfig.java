package app.config;

import app.dtos.weather.WeatherForecastDTO;
import app.enums.UserRole;
import app.integrations.WeatherClient;
import app.persistence.entities.Allergen;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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



        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String url = properties.getProperty("DEEPL_URL");
        String deepLApiKey = System.getenv("DEEPL_APIKEY");
        String geminiApiKey = System.getenv("GEMINI_API_KEY");

        ITranslationService translationService = new DeepLTranslationClient(client, objectMapper, url, deepLApiKey);
        IDishTranslationService dishTranslationService = new DishTranslationService(translationService);
        IAiClient aiClient = new GeminiClient(client, objectMapper, geminiApiKey);
        WeatherClient weatherClient = new WeatherClient(client, objectMapper);

        WeatherForecastDTO weatherForecastDTO = weatherClient.getWeatherForecast();
        System.out.println(weatherForecastDTO);


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

        User u1 = new User("Gordon", "Ramsay", "gordon@kitchen.com", "hash1", UserRole.HEAD_CHEF);
        User u2 = new User("Claire", "Smyth", "claire@pastry.com", "hash2", UserRole.LINE_COOK);

        Station s1 = new Station("Cold Kitchen", "Salads & Starters");
        Set<Allergen> allergens = new HashSet<>();
        Allergen gluten = new Allergen("Gluten", "Cereals containing gluten", 1);
        Allergen dairy = new Allergen("Dairy", "Milk and products thereof (including lactose)",2);
        Allergen eggs = new Allergen("Eggs", "Eggs and products thereof",3);
        allergens.add(gluten);
        allergens.add(dairy);
        allergens.add(eggs);

        DishSuggestion d1 = new DishSuggestion(
            "Røget Laks",
            "Laks med dildcreme og rugbrødschips",
            7,
            2026,
            s1,
            u1,
            allergens
        );

        //DishTranslationDTO dishTranslationDTO = dishTranslationService.translateTo(d1, "DE");

        //System.out.println(dishTranslationDTO);

        //Map<String, String> result = aiClient.normalizeIngredientList(ingredientsToNormalize, "da");

        //result.forEach((k, v) -> System.out.println("Key: " + k + " Value " + v));


    }
}
