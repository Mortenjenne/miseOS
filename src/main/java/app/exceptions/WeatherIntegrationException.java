package app.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherIntegrationException extends RuntimeException
{
    private static final Logger logger = LoggerFactory.getLogger(WeatherIntegrationException.class);
    public WeatherIntegrationException(String message)
    {
        super(message);
        logger.error("WeatherIntegrationException : {}", message);
    }
}
