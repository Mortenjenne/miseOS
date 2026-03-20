package app.controllers;

import app.dtos.security.AuthenticatedUser;
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
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);

        CreateShoppingListDTO dto = ctx.bodyValidator(CreateShoppingListDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        ShoppingListDTO shoppingListDTO = shoppingListService.generateShoppingList(authUser, dto);
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
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long listId = RequestUtil.requirePathId(ctx,"id");

        boolean isDeleted = shoppingListService.deleteShoppingList(authUser, listId);
        ctx.status(isDeleted ? 204 : 404);
    }

    @Override
    public void updateDeliveryDate(Context ctx)
    {
        Long listId = RequestUtil.requirePathId(ctx, "id");

        UpdateShoppingListDTO dto = ctx.bodyValidator(UpdateShoppingListDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        ShoppingListDTO shoppingListDTO = shoppingListService.updateDeliveryDate(listId, dto);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void getShoppingLists(Context ctx)
    {
        ShoppingListStatus status = RequestUtil.getQueryShoppingListStatus(ctx, "status");
        LocalDate deliveryDate = RequestUtil.getQueryDate(ctx, "deliveryDate");

        List<ShoppingListDTO> shoppingLists = shoppingListService.getShoppingLists(status, deliveryDate);
        ctx.status(200).json(shoppingLists);
    }

    @Override
    public void addShoppingListItem(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long listId = RequestUtil.requirePathId(ctx, "id");

        CreateShoppingListItemDTO dto = ctx.bodyValidator(CreateShoppingListItemDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        ShoppingListDTO shoppingListDTO = shoppingListService.addItemToShoppingList(authUser, listId, dto);
        ctx.status(201).json(shoppingListDTO);
    }

    @Override
    public void removeShoppingListItem(Context ctx)
    {
        Long listId = RequestUtil.requirePathId(ctx,"id");
        Long itemId = RequestUtil.requirePathId(ctx,"itemId");

        ShoppingListDTO shoppingListDTO = shoppingListService.removeItem(listId, itemId);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void updateShoppingListItem(Context ctx)
    {
        Long listId = RequestUtil.requirePathId(ctx, "id");
        Long itemId = RequestUtil.requirePathId(ctx, "itemId");

        UpdateShoppingListItemDTO dto = ctx.bodyValidator(UpdateShoppingListItemDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        ShoppingListDTO shoppingListDTO = shoppingListService.updateItem(listId, itemId, dto);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void markShoppingListItemOrdered(Context ctx)
    {
        Long listId = RequestUtil.requirePathId(ctx, "id");
        Long itemId = RequestUtil.requirePathId(ctx, "itemId");

        ShoppingListDTO shoppingListDTO = shoppingListService.markItemOrdered(listId, itemId);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void markAllShoppinglistItemOrdered(Context ctx)
    {
        Long listId = RequestUtil.requirePathId(ctx, "id");

        ShoppingListDTO shoppingListDTO = shoppingListService.markAllItemsOrdered(listId);
        ctx.status(200).json(shoppingListDTO);
    }

    @Override
    public void finalizeShoppingList(Context ctx)
    {
        Long id = RequestUtil.requirePathId(ctx, "id");

        ShoppingListDTO shoppingListDTO = shoppingListService.finalizeShoppingList(id);
        ctx.status(200).json(shoppingListDTO);
    }
}
