package app.services;

import app.dtos.station.StationDTO;
import app.dtos.station.StationRequestDTO;

import java.util.Set;

public interface IStationService
{
    StationDTO createStation(Long creatorID, StationRequestDTO dto);

    StationDTO updateStation(Long editorId, Long stationId, StationRequestDTO dto);

    boolean deleteStation(Long userId, Long stationId);

    StationDTO getStationById(Long id);

    Set<StationDTO> getAllStations();

    StationDTO getStationByName(String name);
}
