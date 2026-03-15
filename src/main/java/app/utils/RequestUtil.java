package app.utils;

import app.enums.MenuStatus;
import app.enums.RequestType;
import app.enums.Status;
import app.enums.SupportedLanguage;
import io.javalin.http.Context;

import java.time.LocalDate;


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
            .check(v -> v != null && !v.isBlank(), "Path parameter '" + param + "' is required")
            .get().trim();
    }

    public static String requireQueryString(Context ctx, String param)
    {
        return ctx.queryParamAsClass(param, String.class)
            .check(v -> v != null && !v.isBlank(),"Query parameter '" + param + "' is required")
            .get().trim();
    }

    public static Integer requireQueryInt(Context ctx, String param)
    {
        return ctx.queryParamAsClass(param, Integer.class)
            .check(v -> v != null && v > 0, param + " must be positive")
            .get();
    }

    public static Integer getQueryInt(Context ctx, String param)
    {
        if (!isPresent(ctx, param)) return null;

        return requireQueryInt(ctx, param);
    }

    public static Long getQueryLong(Context ctx, String param)
    {
        if (!isPresent(ctx, param)) return null;

        return ctx.queryParamAsClass(param, Long.class)
            .check(v -> v > 0, param + " must be positive")
            .get();
    }

    public static String getQueryString(Context ctx, String param)
    {
        if (!isPresent(ctx, param)) return null;

        return requireQueryString(ctx, param);
    }

    public static MenuStatus getQueryMenuStatus(Context ctx, String param)
    {
        if (!isPresent(ctx, param)) return null;

        try
        {
            return MenuStatus.valueOf(ctx.queryParam(param).trim().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Invalid menu status: " + ctx.queryParam(param));
        }
    }

    public static RequestType getQueryRequestType(Context ctx, String param)
    {
        if (!isPresent(ctx, param)) return null;

        try
        {
            return RequestType.valueOf(ctx.queryParam(param).trim().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Invalid request type: " + ctx.queryParam(param));
        }
    }

    public static LocalDate getQueryDate(Context ctx, String param)
    {
        if (!isPresent(ctx, param)) return null;

        try
        {
            return LocalDate.parse(ctx.queryParam(param).trim());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Invalid date format: " + ctx.queryParam(param));
        }
    }

    public static SupportedLanguage getQueryLanguage(Context ctx, String param)
    {
        String value = getQueryString(ctx, param);
        return SupportedLanguage.fromCode(value);
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

    public static Boolean getQueryBoolean(Context ctx, String param)
    {
        if (!isPresent(ctx, param)) return null;

        String value = ctx.queryParam(param).trim().toLowerCase();

        return switch (value)
        {
            case "true"  -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Invalid value for '" + param + "': expected true or false, got: " + value);
        };
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

    private static boolean isPresent(Context ctx, String param)
    {
        String value = ctx.queryParam(param);
        return value != null && !value.isBlank();
    }


}
