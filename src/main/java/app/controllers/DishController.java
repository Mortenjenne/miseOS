package app.controllers;

import app.dtos.dish.*;
import app.dtos.security.AuthenticatedUser;
import app.services.IDishService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DishController implements IDishController
{
    private final IDishService dishService;

    public DishController(IDishService dishService)
    {
        this.dishService = dishService;
    }

    @Override
    public void search(Context ctx)
    {
        String query = RequestUtil.requireQueryString(ctx, "query");
        List<DishDTO> dishDTOS = dishService.searchByName(query);
        ctx.status(200).json(dishDTOS);
    }

    @Override
    public void getAvailableForMenu(Context ctx)
    {
        Integer week = RequestUtil.requireQueryInt(ctx, "week");
        Integer year = RequestUtil.requireQueryInt(ctx, "year");

        AvailableDishesDTO availableDishesDTO = dishService.getAvailableDishesForMenu(week, year);
        ctx.status(200).json(availableDishesDTO);
    }

    @Override
    public void getAllGrouped(Context ctx)
    {
        Map<String, List<DishOptionDTO>> groupedDishOptions = dishService.getAllActiveDishesGrouped();
        ctx.status(200).json(groupedDishOptions);
    }

    @Override
    public void activate(Context ctx)
    {
        Long dishId = RequestUtil.requirePathId(ctx, "id");
        DishDTO dishDTO = dishService.activate(dishId);
        ctx.status(200).json(dishDTO);
    }

    @Override
    public void deactivate(Context ctx)
    {
        Long dishId = RequestUtil.requirePathId(ctx, "id");
        DishDTO dishDTO = dishService.deactivate(dishId);
        ctx.status(200).json(dishDTO);
    }

    @Override
    public void getById(Context ctx)
    {
        Long id = RequestUtil.requirePathId(ctx, "id");
        DishDTO dishDTO = dishService.getById(id);
        ctx.status(200).json(dishDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        Long stationId = RequestUtil.getQueryLong(ctx,"stationId");
        Boolean active = RequestUtil.getQueryBoolean(ctx, "active");

        List<DishDTO> dishDTOS = dishService.getAll(stationId, active);
        ctx.status(200).json(dishDTOS);
    }

    @Override
    public void create(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);

        DishCreateDTO dishCreateDTO = ctx.bodyValidator(DishCreateDTO.class)
            .check(Objects::nonNull, "Request body cannot be null")
            .get();

        DishDTO dishDTO = dishService.createDish(authUser, dishCreateDTO);
        ctx.status(201).json(dishDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long dishId = RequestUtil.requirePathId(ctx,"id");

        DishUpdateDTO dishUpdateDTO = ctx.bodyValidator(DishUpdateDTO.class)
            .check(Objects::nonNull, "Request body cannot be null")
            .get();

        DishDTO dishDTO = dishService.updateDish(dishId, dishUpdateDTO);
        ctx.status(200).json(dishDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long dishId = RequestUtil.requirePathId(ctx,"id");

        boolean isDeleted = dishService.deleteDish(dishId);
        ctx.status(isDeleted ? 204 : 404);
    }
}
