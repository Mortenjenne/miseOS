package app.config;

import app.dtos.DishTranslationDTO;
import app.enums.UserRole;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.services.DeepLTranslationService;
import app.services.DishTranslationService;
import app.services.IDishTranslationService;
import app.services.ITranslationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.Properties;

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

        ITranslationService translationService = new DeepLTranslationService(client, objectMapper, url, deepLApiKey);
        IDishTranslationService dishTranslationService = new DishTranslationService(translationService);


        Station s1 = new Station("Cold Kitchen", "Salads & Starters");
        User u1 = new User("Gordon", "Ramsay", "gordon@kitchen.com", "hash1", UserRole.HEAD_CHEF);

        DishSuggestion d1 = new DishSuggestion(
            "Røget Laks",
            "Laks med dildcreme og rugbrødschips",
            s1,
            u1
        );

        DishTranslationDTO dishTranslationDTO = dishTranslationService.translateTo(d1, "EN");
        System.out.println(dishTranslationDTO);
    }
}
