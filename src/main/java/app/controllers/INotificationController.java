package app.controllers;

import io.javalin.http.Context;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;

public interface INotificationController
{
    void handleNotifications(WsConfig ws);

    void handleConnect(WsConnectContext ctx);

    void handleClose(WsCloseContext ctx);

    void handleError(WsErrorContext ctx);

    void getSnapshot(Context ctx);
}
