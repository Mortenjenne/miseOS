package app.enums;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole
{
    ANYONE,
    CUSTOMER,
    LINE_COOK,
    CHEF_DE_PARTIE,
    SOUS_CHEF,
    HEAD_CHEF
}
