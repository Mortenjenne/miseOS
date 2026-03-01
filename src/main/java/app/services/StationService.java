package app.services;

import app.dtos.station.StationRequestDTO;
import app.dtos.station.StationDTO;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.mappers.StationMapper;
import app.persistence.daos.interfaces.IStationDAO;
import app.persistence.daos.interfaces.IUserReader;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StationService
{
    private final IStationDAO stationDAO;
    private final IUserReader userReader;

    public StationService(IStationDAO stationDAO, IUserReader userReader)
    {
        this.stationDAO = stationDAO;
        this.userReader = userReader;
    }

    public StationDTO registerStation(Long createdById, StationRequestDTO dto)
    {
        ValidationUtil.validateId(createdById);
        validateInput(dto);

        User creator = userReader.getByID(createdById);
        requireChef(creator);
        validateStationNameUnique(dto.name());

        Station station = new Station(dto.name(), dto.description());
        Station saved = stationDAO.create(station);

        return StationMapper.toDTO(saved);
    }

    public StationDTO updateStation(Long editorId, Long stationId, StationRequestDTO dto)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(stationId);
        ValidationUtil.validateId(editorId);

        User editor = userReader.getByID(editorId);
        Station station = stationDAO.getByID(stationId);
        requireChef(editor);

        if (!station.getStationName().equals(dto.name()))
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

    public boolean deleteStation(Long stationId, Long userId)
    {
        ValidationUtil.validateId(stationId);
        ValidationUtil.validateId(userId);

        Station station = stationDAO.getByID(stationId);
        User user = userReader.getByID(userId);
        requireChef(user);

        return stationDAO.delete(station.getId());
    }

    public StationDTO getStationById(Long id)
    {
        ValidationUtil.validateId(id);
        Station station = stationDAO.getByID(id);

        return StationMapper.toDTO(station);
    }

    public Set<StationDTO> getAllStations()
    {
        return stationDAO.getAll()
            .stream()
            .map(StationMapper::toDTO)
            .collect(Collectors.toSet());
    }

    public StationDTO getStationByName(String name)
    {
        ValidationUtil.validateNotBlank(name, "Station name");

        return stationDAO.findByName(name)
            .map(StationMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + name));
    }

    private void requireChef(User user)
    {
        if (!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef or sous chef can manage stations");
        }
    }

    private void validateInput(StationRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Station request");
        ValidationUtil.validateNotBlank(dto.name(), "Station name");
        ValidationUtil.validateNotBlank(dto.description(), "Description");
    }

    private void validateStationNameUnique(String name)
    {
        Optional<Station> existing = stationDAO.findByName(name);

        if (existing.isPresent())
        {
            throw new ValidationException("Station with name '" + name + "' already exists");
        }
    }
}
