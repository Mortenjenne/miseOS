package app.services;

import app.dtos.station.StationDTO;
import app.dtos.station.StationRequestDTO;

import java.util.Set;

public interface IStationService
{
    StationDTO createStation(StationRequestDTO dto);

    StationDTO updateStation(Long stationId, StationRequestDTO dto);

    boolean deleteStation(Long stationId);

    StationDTO getStationById(Long stationId);

    Set<StationDTO> getAllStations();

    StationDTO getStationByName(String stationName);
}
