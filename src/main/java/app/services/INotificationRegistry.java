package app.services;

import io.javalin.websocket.WsContext;

public interface INotificationRegistry
{
    void registerAdmin(WsContext ctx);
    void unregisterAdmin(WsContext ctx);
    void registerStaff(WsContext ctx, Long userId);
    void unregisterStaff(WsContext ctx);
}
