package app.controllers;

import app.dtos.dishsuggestion.DishSuggestionCreateDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
import app.enums.Status;
import app.persistence.entities.DishSuggestion;
import app.services.IDishSuggestionService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DishSuggestionController implements IDishSuggestionController
{
    private final IDishSuggestionService dishSuggestionService;

    public DishSuggestionController(IDishSuggestionService dishSuggestionService)
    {
        this.dishSuggestionService = dishSuggestionService;
    }

    @Override
    public void approveSuggestion(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");
        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.approveSuggestion(suggestionId, userId);
        ctx.status(200).json(dishSuggestionDTO);
    }

    //TODO FEEDBACK correctly handled?
    @Override
    public void rejectSuggestion(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");
        String feedback = ctx.bodyAsClass(String.class);
        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.rejectSuggestion(suggestionId, userId, feedback);
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void getByIdWithAllergens(Context ctx)
    {
        Long suggestionId = RequestUtil.requirePathId(ctx, "id");
        DishSuggestionDTO dishSuggestionDTO = dishSuggestionService.getByIdWithAllergens(suggestionId);
        ctx.status(200).json(dishSuggestionDTO);
    }

    @Override
    public void getAllPending(Context ctx)
    {
        Set<DishSuggestionDTO> dishSuggestionDTOS = dishSuggestionService.getAllDishSuggestions();
        ctx.status(200).json(dishSuggestionDTOS);
    }

    @Override
    public void getPendingForWeek(Context ctx)
    {
        int week = RequestUtil.requireQueryInt(ctx, "week");
        int year = RequestUtil.requireQueryInt(ctx, "year");

        Set<DishSuggestionDTO> dishSuggestionDTOs = dishSuggestionService.getPendingForWeek(week, year);
        ctx.status(200).json(dishSuggestionDTOs);
    }

    @Override
    public void getApprovedForWeek(Context ctx)
    {
        int week = RequestUtil.requireQueryInt(ctx, "week");
        int year = RequestUtil.requireQueryInt(ctx, "year");

        Set<DishSuggestionDTO> dishSuggestionDTOs = dishSuggestionService.getApprovedForWeek(week, year);
        ctx.status(200).json(dishSuggestionDTOs);
    }

    @Override
    public void getByStatus(Context ctx)
    {
        Status status = RequestUtil.requirePathStatus(ctx, "status");
        Set<DishSuggestionDTO> dishSuggestionDTOs = dishSuggestionService.getByStatus(status);
        ctx.status(200).json(dishSuggestionDTOs);
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
        Set<DishSuggestionDTO> dishSuggestionDTOS = dishSuggestionService.getAllDishSuggestions();
        ctx.status(200).json(dishSuggestionDTOS);
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
}
