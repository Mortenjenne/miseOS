package app.controllers;

import io.javalin.http.Context;

public interface ISecurityController
{
    void login(Context ctx);

    void authenticate(Context ctx);

    void authorize(Context ctx);
}
