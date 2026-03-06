package app.config;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.integrations.ai.GeminiClient;
import app.integrations.ai.IAiClient;
import app.integrations.translation.DeepLTranslationClient;
import app.integrations.translation.ITranslationClient;
import app.integrations.weather.WeatherClient;
import app.services.*;
import app.services.impl.AiService;
import app.services.impl.DishTranslationService;
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





    }
}
