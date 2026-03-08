package app.controllers;

import app.dtos.station.StationDTO;
import app.dtos.station.StationRequestDTO;
import app.services.IStationService;
import app.utils.SecurityUtil;
import io.javalin.http.Context;

import java.util.Set;

public class StationController implements IStationController
{
    private final IStationService stationService;

    public StationController(IStationService stationService)
    {
        this.stationService = stationService;
    }

    @Override
    public void create(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);

        StationRequestDTO dto = ctx.bodyValidator(StationRequestDTO.class)
            .check(s -> s.name() != null && !s.name().isBlank(), "Station name is required")
            .check(s -> s.description() != null && !s.description().isBlank(), "Station description is required")
            .get();

        StationDTO stationDTO = stationService.createStation(userId, dto);
        ctx.status(201).json(stationDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long stationId = requirePathId(ctx);

        StationRequestDTO dto = ctx.bodyValidator(StationRequestDTO.class)
            .check(s -> s.name() != null && !s.name().isBlank(), "Station name is required")
            .check(s -> s.description() != null && !s.description().isBlank(), "Station description is required")
            .get();

        StationDTO stationDTO = stationService.updateStation(userId, stationId, dto);
        ctx.status(200).json(stationDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        Long stationId = requirePathId(ctx);

        boolean isDeleted = stationService.deleteStation(userId, stationId);
        ctx.status(isDeleted ? 204 : 404);
    }

    @Override
    public void getById(Context ctx)
    {
        Long stationId = requirePathId(ctx);
        StationDTO stationDTO = stationService.getStationById(stationId);

        ctx.status(200).json(stationDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        Set<StationDTO> stationDTOS = stationService.getAllStations();
        ctx.status(200).json(stationDTOS);
    }

    @Override
    public void getByName(Context ctx)
    {
        String nameQuery = ctx.pathParam("name");
        StationDTO stationDTO = stationService.getStationByName(nameQuery);
        ctx.status(200).json(stationDTO);
    }

    private Long requirePathId(Context ctx)
    {
        return ctx.pathParamAsClass("id", Long.class)
            .check(i -> i > 0, "ID must be positive")
            .get();
    }
}
