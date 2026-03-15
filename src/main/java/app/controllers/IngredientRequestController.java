package app.controllers;

import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.enums.RequestType;
import app.enums.Status;
import app.services.IIngredientRequestService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class IngredientRequestController implements IIngredientRequestController
{
    private final IIngredientRequestService ingredientRequestService;

    public IngredientRequestController(IIngredientRequestService ingredientRequestService)
    {
        this.ingredientRequestService = ingredientRequestService;
    }

    @Override
    public void approve(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long requestId = RequestUtil.requirePathId(ctx, "id");

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.approveIngredientRequest(userId, requestId);
        ctx.status(200).json(ingredientRequestDTO);
    }

    @Override
    public void reject(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long requestId = RequestUtil.requirePathId(ctx, "id");

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.rejectIngredientRequest(userId, requestId);
        ctx.status(200).json(ingredientRequestDTO);
    }

    @Override
    public void getById(Context ctx)
    {
        Long requestId = RequestUtil.requirePathId(ctx, "id");

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.getById(requestId);
        ctx.status(200).json(ingredientRequestDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Status status = RequestUtil.getQueryStatus(ctx, "status");
        LocalDate deliveryDate = RequestUtil.getQueryDate(ctx, "deliveryDate");
        RequestType requestType = RequestUtil.getQueryRequestType(ctx, "requestType");

        List<IngredientRequestDTO> ingredientRequests = ingredientRequestService.getRequests(userId, status, deliveryDate, requestType);
        ctx.status(200).json(ingredientRequests);
    }

    @Override
    public void create(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        CreateIngredientRequestDTO dto = ctx.bodyValidator(CreateIngredientRequestDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.createIngredientRequest(userId, dto);
        ctx.status(201).json(ingredientRequestDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long requestId = RequestUtil.requirePathId(ctx, "id");
        UpdateIngredientRequestDTO dto = ctx.bodyValidator(UpdateIngredientRequestDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.updateRequest(userId, requestId, dto);
        ctx.status(200).json(ingredientRequestDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long requestId = RequestUtil.requirePathId(ctx,"id");

        boolean isDeleted = ingredientRequestService.deleteRequest(userId, requestId);
        ctx.status(isDeleted ? 204 : 404);
    }
}
