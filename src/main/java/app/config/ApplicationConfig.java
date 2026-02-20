package app.config;

import app.services.DeepLTranslationClient;
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

        ITranslationService translationService = new DeepLTranslationClient(client, objectMapper, url, deepLApiKey);
        IDishTranslationService dishTranslationService = new DishTranslationService(translationService);


    }
}
