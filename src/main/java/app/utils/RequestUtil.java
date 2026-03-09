package app.utils;

import app.enums.Status;
import app.exceptions.ValidationException;
import io.javalin.http.Context;

import java.util.Locale;
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

    public static Integer getQueryInt(Context ctx, String param)
    {
        String value = ctx.queryParam(param);
        if (value == null || value.isBlank())
        {
            return null;
        }

        return ctx.queryParamAsClass(param, Integer.class)
            .check(v -> v > 0, param + " must be positive")
            .get();
    }

    public static Long getQueryLong(Context ctx, String param)
    {
        String value = ctx.queryParam(param);
        if (value == null || value.isBlank())
        {
            return null;
        }

        return ctx.queryParamAsClass(param, Long.class)
            .check(v -> v > 0, param + " must be positive")
            .get();
    }

    public static String getQueryString(Context ctx, String param)
    {
        String value = ctx.queryParam(param);
        if (value == null || value.isBlank())
        {
            return null;
        }

        return ctx.queryParamAsClass(param, String.class)
            .check(v -> v != null && !v.isBlank(), param + " cannot be be blank")
            .get().trim();
    }

    public static Status getQueryStatus(Context ctx, String param)
    {
        String value = ctx.queryParam(param);
        if (value == null || value.isBlank())
        {
            return null;
        }

        return parseStatus(value.trim());
    }

    public static Status parseStatus(String status)
    {
        try
        {
            return Status.valueOf(status.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }
}
