package app.controllers;

import app.dtos.security.LoginRequestDTO;
import app.dtos.security.LoginResponseDTO;
import app.enums.UserRole;
import app.exceptions.AuthenticationException;
import app.exceptions.UnauthorizedActionException;
import app.persistence.entities.User;
import app.services.ISecurityService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController
{
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private final ISecurityService securityService;

    public SecurityController(ISecurityService securityService)
    {
        this.securityService = securityService;
    }

    @Override
    public void login(Context ctx)
    {
        LoginRequestDTO dto = ctx.bodyAsClass(LoginRequestDTO.class);
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

        String token = header.substring(7);
        User user = securityService.verifyAndGetUser(token);

        ctx.attribute("user", user);
        logger.info("[{}] Authenticated: {} role: {}", ctx.attribute("request-id"), user.getEmail(), user.getUserRole().name());
    }

    @Override
    public void authorize(Context ctx)
    {
        Set<String> allowedRoles = getAllowedRoles(ctx);

        if (allowedRoles.isEmpty() || allowedRoles.contains("ANYONE"))
        {
            return;
        }

        User user = ctx.attribute("user");
        requireUserNotNull(user);

        if (allowedRoles.contains("KITCHEN_STAFF"))
        {
            boolean isKitchenStaff = user.getUserRole() == UserRole.HEAD_CHEF || user.getUserRole() == UserRole.SOUS_CHEF || user.getUserRole() == UserRole.LINE_COOK;

            if (!isKitchenStaff)
            {
                throw new UnauthorizedActionException("Kitchen staff only");
            }
            return;
        }

        if (!allowedRoles.contains(user.getUserRole().name()))
        {
            logger.warn("[{}] Authorization failed: {} has role {} but needs {}", ctx.attribute("request-id"), user.getEmail(), user.getUserRole().name(), allowedRoles);
            throw new UnauthorizedActionException("Insufficient role. Required: " + allowedRoles);
        }
    }

    private static void requireUserNotNull(User user)
    {
        if (user == null)
        {
            throw new AuthenticationException("No authenticated user found");
        }
    }

    private Set<String> getAllowedRoles(Context ctx)
    {
        return ctx.routeRoles()
            .stream()
            .map(role -> role.toString().toUpperCase())
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
