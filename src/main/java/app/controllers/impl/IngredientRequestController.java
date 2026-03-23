package app.controllers.impl;

import app.controllers.IIngredientRequestController;
import app.dtos.ingredient.ApproveIngredientRequestDTO;
import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.dtos.security.AuthenticatedUser;
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
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long requestId = RequestUtil.requirePathId(ctx, "id");

        ApproveIngredientRequestDTO dto = null;

        if (!ctx.body().trim().isEmpty())
        {
            dto = ctx.bodyAsClass(ApproveIngredientRequestDTO.class);
        }

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.approveIngredientRequest(authUser, requestId, dto);
        ctx.status(200).json(ingredientRequestDTO);
    }

    @Override
    public void reject(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long requestId = RequestUtil.requirePathId(ctx, "id");

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.rejectIngredientRequest(authUser, requestId);
        ctx.status(200).json(ingredientRequestDTO);
    }

    @Override
    public void getById(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long requestId = RequestUtil.requirePathId(ctx, "id");

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.getById(authUser, requestId);
        ctx.status(200).json(ingredientRequestDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Status status = RequestUtil.getQueryStatus(ctx, "status");
        LocalDate deliveryDate = RequestUtil.getQueryDate(ctx, "deliveryDate");
        RequestType requestType = RequestUtil.getQueryRequestType(ctx, "requestType");
        Long stationId = RequestUtil.getQueryLong(ctx, "stationId");

        List<IngredientRequestDTO> ingredientRequests = ingredientRequestService.getRequests(authUser, status, deliveryDate, requestType, stationId);
        ctx.status(200).json(ingredientRequests);
    }

    @Override
    public void create(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        CreateIngredientRequestDTO dto = ctx.bodyValidator(CreateIngredientRequestDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.createIngredientRequest(authUser, dto);
        ctx.status(201).json(ingredientRequestDTO);
    }

    @Override
    public void update(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long requestId = RequestUtil.requirePathId(ctx, "id");

        UpdateIngredientRequestDTO dto = ctx.bodyValidator(UpdateIngredientRequestDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        IngredientRequestDTO ingredientRequestDTO = ingredientRequestService.updateRequest(authUser, requestId, dto);
        ctx.status(200).json(ingredientRequestDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long requestId = RequestUtil.requirePathId(ctx,"id");

        boolean isDeleted = ingredientRequestService.deleteRequest(authUser, requestId);
        ctx.status(isDeleted ? 204 : 404);
    }
}
