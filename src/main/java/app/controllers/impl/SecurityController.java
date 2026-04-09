package app.controllers.impl;

import app.controllers.ISecurityController;
import app.dtos.security.AuthenticatedUser;
import app.dtos.security.LoginRequestDTO;
import app.dtos.security.LoginResponseDTO;
import app.exceptions.AuthenticationException;
import app.exceptions.UnauthorizedActionException;
import app.services.ISecurityService;
import io.javalin.http.Context;
import io.javalin.security.RouteRole;
import io.javalin.websocket.WsConnectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController
{
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private final ISecurityService securityService;

    public SecurityController(ISecurityService securityService)
    {
        this.securityService = securityService;
    }

    @Override
    public void login(Context ctx)
    {
        LoginRequestDTO dto = ctx.bodyValidator(LoginRequestDTO.class)
            .check(Objects::nonNull, "Login payload cannot be null")
            .get();

        LoginResponseDTO response = securityService.login(dto);

        logger.info("[{}] Login successful: {}", ctx.attribute("request-id"), response.email());
        ctx.status(200).json(response);
    }

    @Override
    public void authenticate(Context ctx)
    {
        if (ctx.method().toString().equals("OPTIONS"))
        {
            ctx.status(200);
            return;
        }

        Set<String> allowedRoles = getAllowedRoles(ctx);

        if (allowedRoles.isEmpty() || allowedRoles.contains("ANYONE"))
        {
            return;
        }

        String header = ctx.header("Authorization");
        validateHeader(header);

        String token = header.substring(BEARER_PREFIX.length()).trim();
        AuthenticatedUser authUser = securityService.verifyAndExtract(token);

        ctx.attribute("authUser", authUser);
        logger.info("[{}] Authenticated: {} role: {}", ctx.attribute("request-id"), authUser.email(), authUser.userRole());
    }

    @Override
    public void authorize(Context ctx)
    {
        Set<String> allowedRoles = getAllowedRoles(ctx);

        if (allowedRoles.isEmpty() || allowedRoles.contains("ANYONE"))
        {
            return;
        }

        AuthenticatedUser authUser = ctx.attribute("authUser");
        requireUserNotNull(authUser);

        boolean hasDirectRole = allowedRoles.contains(authUser.userRole().name());
        boolean hasKitchenStaffRole = allowedRoles.contains("KITCHEN_STAFF") && authUser.isKitchenStaff();

        if (!hasDirectRole && !hasKitchenStaffRole)
        {
            logger.warn("[{}] Authorization failed: {} has role {} but needs {}", ctx.attribute("request-id"), authUser.email(), authUser.userRole(), allowedRoles);
            throw new UnauthorizedActionException("Insufficient role. Required: " + allowedRoles);
        }
    }

    @Override
    public void healthCheck(Context ctx)
    {
        ctx.status(200).json("{\"msg\": \"API is up and running v2\"}");
    }

    @Override
    public void authenticateWebSocket(WsConnectContext wsCtx)
    {
        String header = wsCtx.header("Authorization");
        if (header == null || !header.startsWith("Bearer "))
        {
            wsCtx.closeSession(1008, "Missing or malformed Authorization header");
            return;
        }

        try
        {
            String token = header.substring(7).trim();
            AuthenticatedUser authUser = securityService.verifyAndExtract(token);
            wsCtx.attribute("authUser", authUser);
        }
        catch (AuthenticationException e)
        {
            logger.info("WebSocket auth failed for session {}: {}", wsCtx.sessionId, e.getMessage());
            wsCtx.closeSession(1008, e.getMessage());
        }
        catch (Exception e)
        {
            logger.warn("Unexpected WebSocket auth error for session {}", wsCtx.sessionId, e);
            wsCtx.closeSession(1011, "Internal authentication error");
        }
    }

    private static void requireUserNotNull(AuthenticatedUser authenticatedUser)
    {
        if (authenticatedUser == null)
        {
            throw new AuthenticationException("No authenticated user found");
        }
    }

    private Set<String> getAllowedRoles(Context ctx)
    {
        return ctx.routeRoles()
            .stream()
            .map(RouteRole::toString)
            .map(String::toUpperCase)
            .collect(Collectors.toSet());
    }

    private void validateHeader(String header)
    {
        if (header == null || !header.startsWith("Bearer "))
        {
            throw new AuthenticationException("Missing or malformed Authorization header");
        }
    }
}
