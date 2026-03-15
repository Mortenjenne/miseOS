package app.controllers;

import app.dtos.shopping.*;
import app.enums.ShoppingListStatus;
import app.services.IShoppingListService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class ShoppingListController implements IShoppingListController
{
    private final IShoppingListService shoppingListService;

    public ShoppingListController(IShoppingListService shoppingListService)
    {
        this.shoppingListService = shoppingListService;
    }

    @Override
    public void create(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        CreateShoppingListDTO dto = ctx.bodyValidator(CreateShoppingListDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        ShoppingListDTO shoppingListDTO = shoppingListService.generateShoppingList(userId, dto);
        ctx.status(201).json(shoppingListDTO);

    }

    @Override
    public void getById(Context ctx)
    {
        Long listId = RequestUtil.requirePathId(ctx,"id");

        ShoppingListDTO shoppingListDTO = shoppingListService.getById(listId);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long listId = RequestUtil.requirePathId(ctx,"id");

        boolean isDeleted = shoppingListService.deleteShoppingList(userId, listId);
        ctx.status(isDeleted ? 204 : 404);
    }

    @Override
    public void updateDeliveryDate(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long listId = RequestUtil.requirePathId(ctx, "id");

        UpdateShoppingListDTO dto = ctx.bodyValidator(UpdateShoppingListDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        ShoppingListDTO shoppingListDTO = shoppingListService.updateDeliveryDate(userId, listId, dto);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void getShoppingLists(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        ShoppingListStatus status = RequestUtil.getQueryShoppingListStatus(ctx, "status");
        LocalDate deliveryDate = RequestUtil.getQueryDate(ctx, "deliveryDate");

        List<ShoppingListDTO> shoppingLists = shoppingListService.getShoppingLists(userId, status, deliveryDate);
        ctx.status(200).json(shoppingLists);

    }

    @Override
    public void addShoppingListItem(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long listId = RequestUtil.requirePathId(ctx, "id");
        CreateShoppingListItemDTO dto = ctx.bodyValidator(CreateShoppingListItemDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        ShoppingListDTO shoppingListDTO = shoppingListService.addItemToShoppingList(userId, listId, dto);
        ctx.status(201).json(shoppingListDTO);
    }

    @Override
    public void removeShoppingListItem(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long listId = RequestUtil.requirePathId(ctx,"id");
        Long itemId = RequestUtil.requirePathId(ctx,"itemId");

        ShoppingListDTO shoppingListDTO = shoppingListService.removeItem(userId, listId, itemId);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void updateShoppingListItem(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long listId = RequestUtil.requirePathId(ctx, "id");
        Long itemId = RequestUtil.requirePathId(ctx, "itemId");
        UpdateShoppingListItemDTO dto = ctx.bodyValidator(UpdateShoppingListItemDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        ShoppingListDTO shoppingListDTO = shoppingListService.updateItem(userId, listId, itemId, dto);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void markShoppingListItemOrdered(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long listId = RequestUtil.requirePathId(ctx, "id");
        Long itemId = RequestUtil.requirePathId(ctx, "itemId");

        ShoppingListDTO shoppingListDTO = shoppingListService.markItemOrdered(userId, listId, itemId);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void markAllShoppinglistItemOrdered(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long listId = RequestUtil.requirePathId(ctx, "id");

        ShoppingListDTO shoppingListDTO = shoppingListService.markAllItemsOrdered(userId, listId);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void finalizeShoppingList(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long id = RequestUtil.requirePathId(ctx, "id");

        ShoppingListDTO shoppingListDTO = shoppingListService.finalizeShoppingList(userId, id);
        ctx.status(200).json(shoppingListDTO);
    }
}
