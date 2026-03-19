package app.enums;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole
{
    ANYONE,
    CUSTOMER,
    LINE_COOK,
    SOUS_CHEF,
    HEAD_CHEF,
    KITCHEN_STAFF
}
