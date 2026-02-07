package app.utils;

public class ValidationUtil
{
    public static String validateEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Email kan ikke være tom");
        }


        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            throw new IllegalArgumentException("Ikke gyldig email format");
        }

        return email.trim();
    }

    public static void validatePassword(String password)
    {
        if (password == null || password.length() < 8)
        {
            throw new IllegalArgumentException("Password skal være mindst 8 tegn");
        }

        if (!password.matches(".*[A-Z].*"))
        {
            throw new IllegalArgumentException("Password skal indeholde et stort bogstav");
        }

        if (!password.matches(".*[0-9].*"))
        {
            throw new IllegalArgumentException("Password skal indeholde et tal");
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
            throw new IllegalArgumentException(fieldName + " skal være mindst 2 tegn");
        }

        return name.trim();
    }
}
