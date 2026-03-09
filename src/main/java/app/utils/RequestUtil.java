package app.utils;

import app.enums.Status;
import app.exceptions.ValidationException;
import io.javalin.http.Context;

import java.util.Objects;

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
            .get().trim();
    }

    public static Status requirePathStatus(Context ctx, String param)
    {
        String value = ctx.pathParam(param);

        if(value.isBlank())
        {
            throw new IllegalArgumentException(param + " cannot be blank");
        }

        try
        {
            return Status.valueOf(value.toUpperCase().trim());
        }
        catch (IllegalArgumentException e)
        {
            throw new ValidationException("Invalid status: " + value);
        }
    }

    public static int requireQueryInt(Context ctx, String param)
    {
        return ctx.queryParamAsClass(param, Integer.class)
            .check(Objects::nonNull, param + "cannot be null")
            .check(v -> v > 0, param + "must be positive")
            .get();
    }
}
