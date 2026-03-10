package app.utils;

import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

public class SecurityUtil
{
    private SecurityUtil(){}

    //TODO REMOVE OR REFACTOR when JWT is implemented
    public static Long requireUserId(Context ctx)
    {
        String headerId = ctx.header("X-Dev-User-Id");
        if (headerId != null)
        {
            return Long.parseLong(headerId);
        }

        Long userId = ctx.attribute("userId");
        if (userId == null)
        {
            userId = 1L;
        }
        return userId;
    }
}
