package app.controllers;

import io.javalin.http.Context;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;

public interface ISecurityController
{
    void login(Context ctx);

    void authenticate(Context ctx);

    void authorize(Context ctx);

    void authenticateWebSocket(WsConnectContext ws);
}
