package app.controllers;

import io.javalin.http.Context;

public interface IUserController extends IController
{
    void login(Context ctx);
    void changeRole(Context ctx);
    void changeEmail(Context ctx);
    void changePassword(Context ctx);

}
