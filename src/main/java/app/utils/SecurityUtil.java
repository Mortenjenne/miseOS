package app.utils;

import io.javalin.http.Context;

public class SecurityUtil
{
    private SecurityUtil(){}

    //TODO REMOVE OR REFACTOR when JWT is implemented
    public static Long requireUserId(Context ctx)
    {
        Long userId = ctx.attribute("userId");

        if (userId == null)
        {
            userId = 1L;
        }
        return userId;
    }
}
