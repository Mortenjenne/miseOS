package app.controllers;

import app.dtos.user.CreateUserRequestDTO;
import app.dtos.user.UserDTO;
import app.services.IUserService;
import io.javalin.http.Context;

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

    }

    @Override
    public void changeRole(Context ctx)
    {

    }

    @Override
    public void changeEmail(Context ctx)
    {

    }

    @Override
    public void changePassword(Context ctx)
    {

    }

    @Override
    public void assignToStation(Context ctx)
    {

    }

    @Override
    public void getById(Context ctx)
    {
        Long id = ctx.pathParamAsClass("id", Long.class)
            .check(i -> i > 0, "ID has to be postive")
            .get();

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
            .check(u -> u != null, "Payload cant be null")
            .get();

        UserDTO userDTO = userService.registerUser(dto);
        ctx.status(201);
        ctx.json(userDTO);
    }

    @Override
    public void update(Context ctx)
    {

    }

    @Override
    public void delete(Context ctx)
    {

    }
}
