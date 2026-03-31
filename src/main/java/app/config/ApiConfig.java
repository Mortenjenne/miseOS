package app.config;

import lombok.Getter;

@Getter
public class ApiConfig
{
    private static final String DEEPL_URL = "https://api-free.deepl.com/v2/translate";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast?latitude=55.6759&longitude=12.5655&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weathercode&timezone=Europe/Copenhagen&forecast_days=7";
    private final String deepLUrl;
    private final String deepLApiKey;
    private final String geminiUrl;
    private final String geminiApiKey;
    private final String openMeteoUrl;
    private final String issuer;
    private final String secretKey;
    private final long expirationMs;

    public ApiConfig()
    {
        this.deepLUrl = DEEPL_URL;
        this.deepLApiKey = System.getenv("DEEPL_APIKEY");
        this.geminiUrl = GEMINI_URL;
        this.geminiApiKey = System.getenv("GEMINI_API_KEY");
        this.openMeteoUrl = OPEN_METEO_URL;
        this.issuer = System.getenv("ISSUER");
        this.secretKey = System.getenv("SECRET_KEY");
        this.expirationMs = Long.parseLong(System.getenv("TOKEN_EXPIRE_TIME"));
    }
}
