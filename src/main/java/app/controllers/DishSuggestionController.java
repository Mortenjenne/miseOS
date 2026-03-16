package app.controllers;

import app.dtos.dishsuggestion.*;
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
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");
        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.getById(suggestionId);
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
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

        List<DishSuggestionDTO> suggestionDTOS = dishSuggestionService.getByFilter(filter);
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
        Long userId = SecurityUtil.requireUserId(ctx);

        DishSuggestionCreateDTO dto = ctx.bodyValidator(DishSuggestionCreateDTO.class)
            .check(Objects::nonNull, "Dish suggestion create body cannot be null")
            .get();

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.createSuggestion(userId, dto);
        ctx.status(201).json(dishSuggestionDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long suggestionID = RequestUtil.requirePathId(ctx, "id");

        DishSuggestionUpdateDTO dto = ctx.bodyValidator(DishSuggestionUpdateDTO.class)
            .check(Objects::nonNull, "Dish suggestion update body cannot be null")
            .get();

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.updateSuggestion(userId, suggestionID, dto);
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");

        boolean isDeleted = dishSuggestionService.deleteSuggestion(suggestionId, userId);
        ctx.status(isDeleted ? 204 : 404);
    }

    @Override
    public void approveSuggestion(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");
        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.approveSuggestion(suggestionId, userId);
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void rejectSuggestion(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");

        RejectDishSuggestionDTO dto = ctx.bodyValidator(RejectDishSuggestionDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .check(r -> r.feedback() != null && !r.feedback().isBlank(), "Feedback is required")
            .get();

        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.rejectSuggestion(suggestionId, userId, dto.feedback());
        ctx.status(200).json(dishSuggestionDTO);
    }
}
