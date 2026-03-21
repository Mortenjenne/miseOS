package app.utils;

import app.dtos.security.AuthenticatedUser;
import app.exceptions.AuthenticationException;
import io.javalin.http.Context;

public class SecurityUtil
{
    private SecurityUtil(){}

    public static AuthenticatedUser getAuthenticatedUser(Context ctx)
    {
        AuthenticatedUser authUser = ctx.attribute("authUser");

        if (authUser == null)
        {
            throw new AuthenticationException("No authenticated user found");
        }
        if (authUser.userId() == null || authUser.userId() <= 0)
        {
            throw new AuthenticationException("Authenticated user id is invalid");
        }

        if (authUser.email() == null || authUser.email().isBlank())
        {
            throw new AuthenticationException("Authenticated user email is invalid");
        }

        return authUser;
    }

    public static AuthenticatedUser getOptionalAuthenticatedUser(Context ctx)
    {
        return ctx.attribute("authUser");
    }
}
