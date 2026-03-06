package app.mappers;

import app.dtos.station.StationDTO;
import app.persistence.entities.Station;

public class StationMapper
{
    private StationMapper() {}

    public static StationDTO toDTO(Station station)
    {
        return new StationDTO(
            station.getId(),
            station.getStationName(),
            station.getDescription()
        );
    }
}
