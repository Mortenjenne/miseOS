package app.config;

import app.dtos.dish.DishTranslationDTO;
import app.enums.UserRole;
import app.persistence.entities.Allergen;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.services.DeepLTranslationClient;
import app.services.DishTranslationService;
import app.services.IDishTranslationService;
import app.services.ITranslationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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
        String url = properties.getProperty("DEEPL_URL");
        String deepLApiKey = System.getenv("DEEPL_APIKEY");
        String geminiApiKey = System.getenv("GEMINI_API_KEY");

        ITranslationService translationService = new DeepLTranslationClient(client, objectMapper, url, deepLApiKey);
        IDishTranslationService dishTranslationService = new DishTranslationService(translationService);
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


    }
}
