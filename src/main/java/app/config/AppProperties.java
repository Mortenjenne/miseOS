package app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties
{
    private static final Properties properties = new Properties();

    static
    {
        try (
            InputStream input = ApplicationConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties"))
        {

            properties.load(input);

        } catch (
            IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String get(String key)
    {
        return properties.getProperty(key);
    }
}

