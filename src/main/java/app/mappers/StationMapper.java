package app.mappers;

import app.dtos.station.StationDTO;
import app.dtos.station.StationReferenceDTO;
import app.persistence.entities.Station;

public class StationMapper
{
    private StationMapper() {}

    public static StationDTO toDTO(Station station)
    {
        if(station == null) return null;

        return new StationDTO(
            station.getId(),
            station.getStationName(),
            station.getDescription()
        );
    }

    public static StationReferenceDTO toReferenceDTO(Station station)
    {
        if(station == null) return null;

        return new StationReferenceDTO(
            station.getId(),
            station.getStationName()
        );
    }
}
