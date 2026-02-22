package app.exceptions;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class TranslationException extends RuntimeException
{
    private static final Logger logger = LoggerFactory.getLogger(TranslationException.class);
    public TranslationException(String message)
    {
        super(message);
        logger.error("TranslationException : {}", message);
    }
}
