package app.controllers.impl;

import app.controllers.ITakeAwayOfferController;
import app.dtos.security.AuthenticatedUser;
import app.dtos.takeaway.TakeAwayOfferCreateDTO;
import app.dtos.takeaway.TakeAwayOfferDTO;
import app.dtos.takeaway.TakeAwayOfferUpdateDTO;
import app.services.ITakeAwayOfferService;
import app.utils.RequestUtil;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class TakeAwayOfferController implements ITakeAwayOfferController
{
    private final ITakeAwayOfferService takeAwayOfferService;

    public TakeAwayOfferController(ITakeAwayOfferService takeAwayOfferService)
    {
        this.takeAwayOfferService = takeAwayOfferService;
    }

    @Override
    public void enableOffer(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long offerId = RequestUtil.requirePathId(ctx, "id");

        TakeAwayOfferDTO takeAwayOfferDTO = takeAwayOfferService.enableOffer(authUser, offerId);
        ctx.status(200).json(takeAwayOfferDTO);
    }

    @Override
    public void disableOffer(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long offerId = RequestUtil.requirePathId(ctx, "id");

        TakeAwayOfferDTO takeAwayOfferDTO = takeAwayOfferService.disableOffer(authUser, offerId);
        ctx.status(200).json(takeAwayOfferDTO);
    }

    @Override
    public void getById(Context ctx)
    {
        Long offerId = RequestUtil.requirePathId(ctx, "id");
        TakeAwayOfferDTO takeAwayOfferDTO = takeAwayOfferService.getById(offerId);
        ctx.status(200).json(takeAwayOfferDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        LocalDate date = RequestUtil.getQueryDate(ctx, "date");
        Boolean isSoldOut = RequestUtil.getQueryBoolean(ctx, "soldOut");
        Boolean isEnabled = RequestUtil.getQueryBoolean(ctx, "enabled");
        Long dishId = RequestUtil.getQueryLong(ctx, "dishId");

        List<TakeAwayOfferDTO> offers = takeAwayOfferService.getOffers(date, isSoldOut, isEnabled, dishId);
        ctx.status(200).json(offers);
    }

    @Override
    public void create(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);

        TakeAwayOfferCreateDTO dto = ctx.bodyValidator(TakeAwayOfferCreateDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        TakeAwayOfferDTO takeAwayOfferDTO = takeAwayOfferService.createOffer(authUser, dto);
        ctx.status(201).json(takeAwayOfferDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long offerId = RequestUtil.requirePathId(ctx, "id");

        TakeAwayOfferUpdateDTO dto = ctx.bodyValidator(TakeAwayOfferUpdateDTO.class)
            .check(Objects::nonNull, "Body cannot be null")
            .get();

        TakeAwayOfferDTO takeAwayOfferDTO = takeAwayOfferService.updateOffer(offerId, dto);
        ctx.status(200).json(takeAwayOfferDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        AuthenticatedUser authUser = SecurityUtil.getAuthenticatedUser(ctx);
        Long offerId = RequestUtil.requirePathId(ctx, "id");

        boolean deleted = takeAwayOfferService.deleteOffer(authUser, offerId);
        ctx.status(deleted ? 204 : 404);
    }
}
