package app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig
{
    private static AppConfig instance;
    private final Properties properties = new Properties();

    private AppConfig()
    {
        load();
    }

    public static AppConfig getInstance()
    {
        if (instance == null)
        {
            instance = new AppConfig();
        }
        return instance;
    }

    private void load()
    {
        try (InputStream input = getClass()
            .getClassLoader()
            .getResourceAsStream("config.properties"))
        {
            if (input == null)
            {
                throw new RuntimeException("config.properties" + " not found in classpath");
            }
            properties.load(input);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load " + "config.properties", e);
        }
    }

    public String get(String key)
    {
        String value = properties.getProperty(key);
        if (value == null)
        {
            throw new RuntimeException("Missing config key: '" + key + "'");
        }
        return value.trim();
    }

    public int getInt(String key)
    {
        try
        {
            return Integer.parseInt(get(key));
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeException("Config key '" + key + "' is not a valid integer");
        }
    }
}
