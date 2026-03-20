package app.controllers;

import app.dtos.dishsuggestion.*;
import app.dtos.security.AuthenticatedUser;
import app.enums.Status;
import app.services.IDishSuggestionService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;

public class DishSuggestionController implements IDishSuggestionController
{
    private final IDishSuggestionService dishSuggestionService;

    public DishSuggestionController(IDishSuggestionService dishSuggestionService)
    {
        this.dishSuggestionService = dishSuggestionService;
    }

    @Override
    public void getById(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.getById(authUser, suggestionId);
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Status status = RequestUtil.getQueryStatus(ctx, "status");
        Integer week = RequestUtil.getQueryInt(ctx, "week");
        Integer year = RequestUtil.getQueryInt(ctx, "year");
        Long stationId = RequestUtil.getQueryLong(ctx, "stationId");
        String orderBy = RequestUtil.getQueryString(ctx, "orderBy");

        DishSuggestionFilterDTO filter = new DishSuggestionFilterDTO(
            status,
            week,
            year,
            stationId,
            orderBy
        );

        List<DishSuggestionDTO> suggestionDTOS = dishSuggestionService.getByFilter(authUser, filter);
        ctx.status(200).json(suggestionDTOS);
    }

    @Override
    public void getCurrentWeek(Context ctx)
    {
        Status status = RequestUtil.getQueryStatus(ctx, "status");
        ctx.status(200).json(dishSuggestionService.getCurrentWeek(status));
    }

    @Override
    public void create(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);

        DishSuggestionCreateDTO dto = ctx.bodyValidator(DishSuggestionCreateDTO.class)
            .check(Objects::nonNull, "Dish suggestion create body cannot be null")
            .get();

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.createSuggestion(authUser, dto);
        ctx.status(201).json(dishSuggestionDTO);
    }

    @Override
    public void update(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long suggestionID = RequestUtil.requirePathId(ctx, "id");

        DishSuggestionUpdateDTO dto = ctx.bodyValidator(DishSuggestionUpdateDTO.class)
            .check(Objects::nonNull, "Dish suggestion update body cannot be null")
            .get();

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.updateSuggestion(authUser, suggestionID, dto);
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");

        boolean isDeleted = dishSuggestionService.deleteSuggestion(authUser, suggestionId);
        ctx.status(isDeleted ? 204 : 404);
    }

    @Override
    public void approveSuggestion(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.approveSuggestion(authUser, suggestionId);
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void rejectSuggestion(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");

        RejectDishSuggestionDTO dto = ctx.bodyValidator(RejectDishSuggestionDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .check(r -> r.feedback() != null && !r.feedback().isBlank(), "Feedback is required")
            .get();

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.rejectSuggestion(authUser, suggestionId, dto.feedback());
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void removeAllergen(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");
        Long allergenId = RequestUtil.requirePathId(ctx, "allergenId");

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.removeAllergen(authUser, suggestionId, allergenId);
        ctx.status(204).json(dishSuggestionDTO);
    }
}
