package app.controllers;

import app.dtos.user.*;
import app.enums.UserRole;
import app.services.IUserService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.util.Objects;

public class UserController implements IUserController
{
    private final IUserService userService;

    public UserController(IUserService userService)
    {
        this.userService = userService;
    }

    @Override
    public void login(Context ctx)
    {
        LoginRequestDTO dto = ctx.bodyValidator(LoginRequestDTO.class)
            .check(Objects::nonNull, "Login payload cannot be null")
            .get();

        UserDTO userDTO = userService.login(dto);

        ctx.status(200).json(userDTO);
    }

    @Override
    public void changeRole(Context ctx)
    {
        Long requesterId = SecurityUtil.requireUserId(ctx);
        Long targetUserId = RequestUtil.requirePathId(ctx, "id");

        UserRole role = ctx.bodyAsClass(UserRole.class);
        UserDTO userDTO = userService.changeRole(requesterId, targetUserId, role);

        ctx.status(200).json(userDTO);
    }

    @Override
    public void changeEmail(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);

        String newEmail = ctx.bodyValidator(String.class)
            .check(e -> e != null && !e.isBlank(), "Email cannot be empty")
            .check(e -> e.contains("@"), "Invalid email")
            .get();

        UserDTO userDTO = userService.changeEmail(userId, newEmail);

        ctx.status(200).json(userDTO);
    }

    @Override
    public void changePassword(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);

        ChangeUserPasswordDTO dto = ctx.bodyValidator(ChangeUserPasswordDTO.class)
            .check(Objects::nonNull, "Password payload cannot be null")
            .check(p -> p.currentPassword() != null && !p.currentPassword().isBlank(), "Current password cannot be empty")
            .check(p -> p.newPassword() != null && !p.newPassword().isBlank(), "New password cannot be empty")
            .get();

        UserDTO userDTO = userService.changePassword(userId, dto);

        ctx.status(200).json(userDTO);
    }

    @Override
    public void assignToStation(Context ctx)
    {
        Long requesterId = SecurityUtil.requireUserId(ctx);
        Long targetUserId = RequestUtil.requirePathId(ctx, "id");
        Long stationId = RequestUtil.requirePathId(ctx, "stationId");

        UserDTO userDTO = userService.assignToStation(requesterId, targetUserId, stationId);
        ctx.status(200).json(userDTO);
    }

    @Override
    public void getById(Context ctx)
    {
        Long id = RequestUtil.requirePathId(ctx, "id");

        UserDTO userDTO = userService.findById(id);
        ctx.status(200);
        ctx.json(userDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        ctx.status(200);
        ctx.json(userService.findAll());
    }

    @Override
    public void create(Context ctx)
    {
        CreateUserRequestDTO dto = ctx.bodyValidator(CreateUserRequestDTO.class)
            .check(Objects::nonNull, "Payload cant be null")
            .get();

        UserDTO userDTO = userService.registerUser(dto);
        ctx.status(201);
        ctx.json(userDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long id = RequestUtil.requirePathId(ctx, "id");

        UpdateUserDTO dto = ctx.bodyValidator(UpdateUserDTO.class)
            .check(Objects::nonNull, "Update payload cannot be null")
            .get();

        UserDTO userDTO = userService.update(id, dto);
        ctx.status(200).json(userDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long requesterId = SecurityUtil.requireUserId(ctx);

        Long targetUserId = ctx.pathParamAsClass("id", Long.class)
            .check(i -> i > 0, "ID must be positive")
            .get();

        boolean deleted = userService.delete(requesterId, targetUserId);

        ctx.status(deleted ? 204 : 404);
    }
}
