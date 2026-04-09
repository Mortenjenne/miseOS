package app.controllers.impl;

import app.controllers.ITakeAwayOrderController;
import app.dtos.security.AuthenticatedUser;
import app.dtos.takeaway.TakeAwayOrderCreateDTO;
import app.dtos.takeaway.TakeAwayOrderDTO;
import app.dtos.takeaway.TakeAwaySummaryDTO;
import app.enums.OrderStatus;
import app.services.ITakeAwayOrderService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class TakeAwayOrderController implements ITakeAwayOrderController
{
    private final ITakeAwayOrderService takeAwayOrderService;

    public TakeAwayOrderController(ITakeAwayOrderService takeAwayOrderService)
    {
        this.takeAwayOrderService = takeAwayOrderService;
    }

    @Override
    public void getOrders(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long customerId = RequestUtil.getQueryLong(ctx, "customerId");
        Long offerId = RequestUtil.getQueryLong(ctx, "offerId");
        LocalDate date = RequestUtil.getQueryDate(ctx, "date");
        OrderStatus status = RequestUtil.getQueryOrderStatus(ctx, "status");

        List<TakeAwayOrderDTO> takeAwayOrderDTOS = takeAwayOrderService.getOrders(authUser, customerId, offerId, date, status);
        ctx.status(200).json(takeAwayOrderDTOS);
    }

    @Override
    public void getById(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long orderId = RequestUtil.requirePathId(ctx, "id");

        TakeAwayOrderDTO takeAwayOrderDTO = takeAwayOrderService.getById(authUser, orderId);
        ctx.status(200).json(takeAwayOrderDTO);
    }

    @Override
    public void placeOrder(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getOptionalAuthenticatedUser(ctx);

        TakeAwayOrderCreateDTO dto = ctx.bodyValidator(TakeAwayOrderCreateDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .check(d -> d.takeAwayOrderLines() != null && !d.takeAwayOrderLines().isEmpty(), "Order must have at least one line")
            .get();

        TakeAwayOrderDTO takeAwayOrderDTO = takeAwayOrderService.placeOrder(authUser, dto);
        ctx.status(201).json(takeAwayOrderDTO);
    }

    @Override
    public void markAsPaid(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long orderId = RequestUtil.requirePathId(ctx, "id");

        TakeAwayOrderDTO takeAwayOrderDTO = takeAwayOrderService.markAsPaid(authUser, orderId);
        ctx.status(200).json(takeAwayOrderDTO);
    }

    @Override
    public void cancelOrder(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long orderId = RequestUtil.requirePathId(ctx, "id");

        TakeAwayOrderDTO takeAwayOrderDTO = takeAwayOrderService.cancelOrder(authUser, orderId);
        ctx.status(200).json(takeAwayOrderDTO);
    }

    @Override
    public void getSummary(Context ctx)
    {
        LocalDate date = RequestUtil.getQueryDate(ctx,"date");

        TakeAwaySummaryDTO takeAwaySummaryDTO = takeAwayOrderService.getSummary(date);
        ctx.status(200).json(takeAwaySummaryDTO);
    }
}
