package app.utils;

import app.config.AppConfig;
import app.exceptions.ValidationException;

import java.time.LocalDate;

public class ValidationUtil
{
    private static final String SAFE_TEXT_PATTERN = "^[\\p{L}0-9\\s&,\\-.( )]*$";
    private static final int NAME_MIN = AppConfig.getInstance().getInt("name.min");
    private static final int NAME_MAX = AppConfig.getInstance().getInt("name.max");
    private static final int DESCRIPTION_MIN = AppConfig.getInstance().getInt("description.min");
    private static final int DESCRIPTION_MAX = AppConfig.getInstance().getInt("description.max");
    private static final int USERNAME_MIN = AppConfig.getInstance().getInt("user.name.min");
    private static final int USERNAME_MAX = AppConfig.getInstance().getInt("user.name.max");
    private static final int PASSWORD_MIN = AppConfig.getInstance().getInt("user.password.min");

    private ValidationUtil(){}

    public static void validateNotNull(Object obj, String entityName)
    {
        if (obj == null)
        {
            throw new IllegalArgumentException(entityName + " cannot be null.");
        }
    }

    public static void validateNotBlank(String value, String fieldName)
    {
        if (value == null || value.isBlank())
        {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }

    }

    public static void validatePositive(double value, String fieldName)
    {
        if (value <= 0)
        {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }

    public static void validateText(String value, String fieldName, int min, int max)
    {
        String trimmed = value.trim();

        if (trimmed.length() < min)
        {
            throw new ValidationException(String.format("%s must be at least %d characters", fieldName, min));
        }

        if (trimmed.length() > max)
        {
            throw new ValidationException(String.format("%s must be at most %d characters",fieldName, max));
        }

        if (!trimmed.matches(SAFE_TEXT_PATTERN)) {

            throw new ValidationException(String.format("%s can only contain letters, numbers, and common symbols like '&', '-', or '.'.", fieldName));
        }
    }

    public static void validateRange(int number, int min, int max, String fieldName)
    {
        if (number < min || number > max)
        {
            String errorMsg = String.format("%s must be between %d and %d, got: %d", fieldName, min, max, number);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public static void validateNotEmpty(java.util.Collection<?> collection, String fieldName)
    {
        if (collection == null || collection.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    public static void validateFutureDate(LocalDate date, String fieldName)
    {
        if (date == null)
        {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " must be in the future");
        }
    }

    public static void validateId(Long id)
    {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID: must be provided and greater than 0");
        }
    }

    public static String validateEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Email kan ikke være tom");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            throw new ValidationException("Ikke gyldig email format");
        }

        return email.trim().toLowerCase();
    }
}
