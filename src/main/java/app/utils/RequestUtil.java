package app.utils;

import io.javalin.http.Context;

public final class RequestUtil
{
    private RequestUtil(){}

    public static Long requirePathId(Context ctx, String param)
    {
        return ctx.pathParamAsClass(param, Long.class)
            .check(i -> i > 0, param + " must be positive")
            .get();
    }

    public static String requirePathString(Context ctx, String param)
    {
        return ctx.pathParamAsClass(param, String.class)
            .check(v -> v != null && !v.isBlank(), param + " cannot be be blank")
            .get();
    }
}
