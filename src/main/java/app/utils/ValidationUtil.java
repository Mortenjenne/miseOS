package app.utils;

import app.exceptions.ValidationException;

import java.time.LocalDate;

public class ValidationUtil
{
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

    public static void validatePassword(String password)
    {
        if (password == null || password.length() < 8)
        {
            throw new ValidationException("Password skal være mindst 8 tegn");
        }

        if (!password.matches(".*[A-Z].*"))
        {
            throw new ValidationException("Password skal indeholde et stort bogstav");
        }

        if (!password.matches(".*[0-9].*"))
        {
            throw new ValidationException("Password skal indeholde et tal");
        }
    }

    public static String validateName(String name, String fieldName)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " kan ikke være tomt");
        }

        if (name.length() < 2)
        {
            throw new ValidationException(fieldName + " skal være mindst 2 tegn");
        }

        return name.trim();
    }
}
