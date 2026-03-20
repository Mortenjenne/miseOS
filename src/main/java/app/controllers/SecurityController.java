package app.controllers;

import app.dtos.security.AuthenticatedUser;
import app.dtos.security.LoginRequestDTO;
import app.dtos.security.LoginResponseDTO;
import app.enums.UserRole;
import app.exceptions.AuthenticationException;
import app.exceptions.UnauthorizedActionException;
import app.services.ISecurityService;
import io.javalin.http.Context;
import io.javalin.security.RouteRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
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

        String token = header.substring(7);
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

        if (allowedRoles.contains("KITCHEN_STAFF"))
        {
            if (!isKitchenStaff(authUser))
            {
                throw new UnauthorizedActionException("Kitchen staff only");
            }
            return;
        }

        if (!allowedRoles.contains(authUser.userRole().name()))
        {
            logger.warn("[{}] Authorization failed: {} has role {} but needs {}", ctx.attribute("request-id"), authUser.email(), authUser.userRole(), allowedRoles);
            throw new UnauthorizedActionException("Insufficient role. Required: " + allowedRoles);
        }
    }

    private static void requireUserNotNull(AuthenticatedUser authenticatedUser)
    {
        if (authenticatedUser == null)
        {
            throw new AuthenticationException("No authenticated user found");
        }
    }

    private boolean isKitchenStaff(AuthenticatedUser authenticatedUser)
    {
        return authenticatedUser.userRole() == UserRole.HEAD_CHEF || authenticatedUser.userRole() == UserRole.SOUS_CHEF || authenticatedUser.userRole() == UserRole.LINE_COOK;
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
