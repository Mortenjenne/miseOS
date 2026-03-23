package app.config;

import lombok.Getter;

@Getter
public class ApiConfig
{
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
        this.deepLUrl = AppProperties.get("DEEPL_URL");
        this.deepLApiKey = System.getenv("DEEPL_APIKEY");
        this.geminiUrl = AppProperties.get("GEMINI_URL");
        this.geminiApiKey = System.getenv("GEMINI_API_KEY");
        this.openMeteoUrl = AppProperties.get("OPEN_METEO_URL");
        this.issuer = AppProperties.get("ISSUER");
        this.secretKey = System.getenv("SECRET_KEY");
        this.expirationMs = Long.parseLong(AppProperties.get("EXPIRATION_MS"));
    }
}
