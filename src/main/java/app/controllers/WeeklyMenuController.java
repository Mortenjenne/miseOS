package app.controllers;

import app.dtos.menu.*;
import app.dtos.security.AuthenticatedUser;
import app.enums.MenuStatus;
import app.enums.SupportedLanguage;
import app.services.IWeeklyMenuService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;

public class WeeklyMenuController implements IWeeklyMenuController
{
    private final IWeeklyMenuService weeklyMenuService;

    public WeeklyMenuController(IWeeklyMenuService weeklyMenuService)
    {
        this.weeklyMenuService = weeklyMenuService;
    }

    @Override
    public void addMenuSlot(Context ctx)
    {
        Long menuId = RequestUtil.requirePathId(ctx, "id");

        AddMenuSlotDTO dto = ctx.bodyValidator(AddMenuSlotDTO.class)
            .check(Objects::nonNull, "Add menu slot body cannot be null")
            .get();

        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.addMenuSlot(menuId, dto);
        ctx.status(201).json(weeklyMenuDTO);
    }

    @Override
    public void removeMenuSlot(Context ctx)
    {
        Long menuId = RequestUtil.requirePathId(ctx, "id");
        Long slotId = RequestUtil.requirePathId(ctx, "slotId");

        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.removeSlot(menuId, slotId);
        ctx.status(200).json(weeklyMenuDTO);
    }

    @Override
    public void updateMenuSlot(Context ctx)
    {
        Long menuId = RequestUtil.requirePathId(ctx, "id");
        Long slotId = RequestUtil.requirePathId(ctx, "slotId");

        UpdateMenuSlotDTO dto = ctx.bodyValidator(UpdateMenuSlotDTO.class)
            .check(Objects::nonNull, "Add menu slot body cannot be null")
            .get();

        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.updateSlot(menuId, slotId, dto);
        ctx.status(200).json(weeklyMenuDTO);
    }

    @Override
    public void translateMenu(Context ctx)
    {
        Long menuId = RequestUtil.requirePathId(ctx, "id");
        SupportedLanguage language = RequestUtil.getQueryLanguage(ctx, "lang");

        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.translateMenu(menuId, language);
        ctx.status(200).json(weeklyMenuDTO);
    }

    @Override
    public void publishMenu(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long menuId = RequestUtil.requirePathId(ctx, "id");

        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.publishMenu(authUser, menuId);
        ctx.status(200).json(weeklyMenuDTO);
    }

    @Override
    public void getCurrentWeekMenu(Context ctx)
    {
        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.getCurrentWeekMenu();
        ctx.status(200).json(weeklyMenuDTO);
    }

    @Override
    public void getByWeekAndYear(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getOptionalAuthenticatedUser(ctx);
        Integer week = RequestUtil.requireQueryInt(ctx, "week");
        Integer year = RequestUtil.requireQueryInt(ctx, "year");

        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.getByWeekAndYear(authUser, week, year);
        ctx.status(200).json(weeklyMenuDTO);
    }

    @Override
    public void translateSlot(Context ctx)
    {
        Long menuId  = RequestUtil.requirePathId(ctx, "id");
        Long slotId  = RequestUtil.requirePathId(ctx, "slotId");
        SupportedLanguage language = RequestUtil.getQueryLanguage(ctx, "lang");

        WeeklyMenuDTO dto = weeklyMenuService.translateSlot(menuId, slotId, language);
        ctx.status(200).json(dto);
    }

    @Override
    public void getById(Context ctx)
    {
        Long menuId = RequestUtil.requirePathId(ctx, "id");
        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.getById(menuId);
        ctx.status(200).json(weeklyMenuDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        MenuStatus status = RequestUtil.getQueryMenuStatus(ctx, "status");
        Integer year = RequestUtil.getQueryInt(ctx, "year");
        Integer week = RequestUtil.getQueryInt(ctx, "week");

        List<WeeklyMenuOverviewDTO> menus = weeklyMenuService.getOverview(status, year, week);

        ctx.status(200).json(menus);
    }

    @Override
    public void create(Context ctx)
    {
        CreateWeeklyMenuDTO dto = ctx.bodyValidator(CreateWeeklyMenuDTO.class)
            .check(Objects::nonNull, "Create weekly menu body cannot be null")
            .get();

        WeeklyMenuDTO weeklyMenuDTO = weeklyMenuService.createMenu(dto);
        ctx.status(201).json(weeklyMenuDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long menuId = RequestUtil.requirePathId(ctx, "id");

        boolean isDeleted = weeklyMenuService.deleteMenu(authUser, menuId);

        ctx.status(isDeleted ? 204 : 404);
    }
}
