package app.controllers;

import io.javalin.http.Context;
import io.javalin.websocket.WsConnectContext;

public interface ISecurityController
{
    void login(Context ctx);

    void authenticate(Context ctx);

    void authorize(Context ctx);

    void healthCheck(Context ctx);

    void authenticateWebSocket(WsConnectContext ws);
}
