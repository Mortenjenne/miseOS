package app.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil
{
    public static String hashPassword(String plainPassword)
    {
        if (plainPassword == null || plainPassword.isEmpty())
        {
            throw new IllegalArgumentException("Password kan ikke være tomt");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword)
    {
        if (plainPassword == null || plainPassword == null)
        {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
