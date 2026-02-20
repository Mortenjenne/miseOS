package app.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIIntegrationException extends RuntimeException
{
    private static final Logger logger = LoggerFactory.getLogger(AIIntegrationException.class);
    public AIIntegrationException(String message)
    {
        super(message);
        logger.error("AIIntegrationException : {}", message);
    }
}
