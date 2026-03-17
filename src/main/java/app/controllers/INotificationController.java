package app.controllers;

import io.javalin.websocket.WsContext;

public interface INotificationController
{
    void registerAdmin(WsContext ctx);
    void registerStaff(WsContext ctx, Long userId);
    void onClose(WsContext ctx);
}
