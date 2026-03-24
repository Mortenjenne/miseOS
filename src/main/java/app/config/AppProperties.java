package app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties
{
    private static final Properties properties = new Properties();

    static
    {
        try (InputStream input = AppProperties.class.getClassLoader().getResourceAsStream("config.properties"))
        {
            if (input == null)
            {
                throw new IllegalStateException("Missing config.properties on classpath (expected in src/main/resources).");
            }

            properties.load(input);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed loading config.properties", e);
        }
    }

    public static String get(String key)
    {
        return properties.getProperty(key);
    }
}
