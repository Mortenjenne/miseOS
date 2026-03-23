package app.services.impl;

import app.dtos.station.StationRequestDTO;
import app.dtos.station.StationDTO;
import app.exceptions.ConflictException;
import app.mappers.StationMapper;
import app.persistence.daos.interfaces.IStationDAO;
import app.persistence.entities.Station;
import app.services.IStationService;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StationService implements IStationService
{
    private final IStationDAO stationDAO;

    public StationService(IStationDAO stationDAO)
    {
        this.stationDAO = stationDAO;
    }

    @Override
    public StationDTO createStation(StationRequestDTO dto)
    {
        validateInput(dto);
        validateStationNameUnique(dto.name());

        Station station = new Station(dto.name(), dto.description());
        Station saved = stationDAO.create(station);

        return StationMapper.toDTO(saved);
    }

    @Override
    public StationDTO updateStation(Long stationId, StationRequestDTO dto)
    {
        ValidationUtil.validateId(stationId);
        validateInput(dto);

        Station station = stationDAO.getByID(stationId);

        if (!station.getStationName().equalsIgnoreCase(dto.name()))
        {
            validateStationNameUnique(dto.name());
        }

        station.update(
            dto.name(),
            dto.description()
        );

        Station updated = stationDAO.update(station);
        return StationMapper.toDTO(updated);
    }

    @Override
    public boolean deleteStation(Long stationId)
    {
        ValidationUtil.validateId(stationId);

        boolean isStationUsed = stationDAO.isUsedByAnyDish(stationId);

        if (isStationUsed)
        {
            throw new ConflictException("Cannot delete station, it is currently assigned to one or more dishes");
        }

        Station station = stationDAO.getByID(stationId);
        return stationDAO.delete(station.getId());
    }

    @Override
    public StationDTO getStationById(Long id)
    {
        ValidationUtil.validateId(id);
        Station station = stationDAO.getByID(id);

        return StationMapper.toDTO(station);
    }

    @Override
    public Set<StationDTO> getAllStations()
    {
        return stationDAO.getAll()
            .stream()
            .map(StationMapper::toDTO)
            .collect(Collectors.toSet());
    }

    @Override
    public StationDTO getStationByName(String name)
    {
        ValidationUtil.validateNotBlank(name, "Station name");
        ValidationUtil.validateRange(name.trim().length(), 2, 100, "Search query length");

        return stationDAO.findByName(name)
            .map(StationMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + name));
    }

    private void validateInput(StationRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Station request");
        ValidationUtil.validateName(dto.name(), "Station name");
        ValidationUtil.validateDescription(dto.description(), "Description");
    }

    private void validateStationNameUnique(String name)
    {
        Optional<Station> existing = stationDAO.findByName(name);

        if (existing.isPresent())
        {
            throw new ConflictException("Station with name '" + name + "' already exists");
        }
    }
}
