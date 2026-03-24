package app.controllers.impl;

import app.controllers.IStationController;
import app.dtos.station.StationDTO;
import app.dtos.station.StationRequestDTO;
import app.services.IStationService;
import app.utils.RequestUtil;
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
        StationRequestDTO dto = ctx.bodyValidator(StationRequestDTO.class)
            .check(s -> s.name() != null && !s.name().isBlank(), "Station name is required")
            .check(s -> s.description() != null && !s.description().isBlank(), "Station description is required")
            .get();

        StationDTO stationDTO = stationService.createStation(dto);
        ctx.status(201).json(stationDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long stationId = RequestUtil.requirePathId(ctx,"id");

        StationRequestDTO dto = ctx.bodyValidator(StationRequestDTO.class)
            .check(s -> s.name() != null && !s.name().isBlank(), "Station name is required")
            .check(s -> s.description() != null && !s.description().isBlank(), "Station description is required")
            .get();

        StationDTO stationDTO = stationService.updateStation(stationId, dto);
        ctx.status(200).json(stationDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long stationId = RequestUtil.requirePathId(ctx,"id");

        boolean isDeleted = stationService.deleteStation(stationId);
        ctx.status(isDeleted ? 204 : 404);
    }

    @Override
    public void getById(Context ctx)
    {
        Long stationId = RequestUtil.requirePathId(ctx, "id");
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
        String nameQuery = RequestUtil.requirePathString(ctx, "name");
        StationDTO stationDTO = stationService.getStationByName(nameQuery);
        ctx.status(200).json(stationDTO);
    }
}
