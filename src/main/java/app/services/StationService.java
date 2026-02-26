package app.services;

import app.dtos.station.StationCreateRequestDTO;
import app.dtos.station.StationDTO;
import app.dtos.station.StationUpdateRequestDTO;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
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

    public StationDTO registerStation(StationCreateRequestDTO dto)
    {
        User creator = userReader.getByID(dto.createdById());

        requireChef(creator);
        validateStationNameUnique(dto.name());

        Station station = new Station(dto.name(), dto.description());
        Station saved = stationDAO.create(station);

        return mapToDTO(saved);
    }

    public StationDTO updateStation(StationUpdateRequestDTO dto)
    {
        ValidationUtil.validateId(dto.stationId());
        ValidationUtil.validateId(dto.editorId());

        User editor = userReader.getByID(dto.editorId());
        Station station = stationDAO.getByID(dto.stationId());
        requireChef(editor);

        station.update(dto.name(), dto.description());

        Station updated = stationDAO.update(station);

        return mapToDTO(updated);
    }

    public boolean deleteStation(Long stationId, Long userId)
    {
        ValidationUtil.validateId(stationId);
        ValidationUtil.validateId(userId);

        Station station = stationDAO.getByID(stationId);
        User user = userReader.getByID(userId);

        requireChef(user);

        //TODO Chef if stations is in use with users or dishes?

        return stationDAO.delete(station.getId());
    }

    public StationDTO getStationById(Long id)
    {
        ValidationUtil.validateId(id);
        Station station = stationDAO.getByID(id);

        return mapToDTO(station);
    }

    public Set<StationDTO> getAllStations()
    {
        return stationDAO.getAll()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    public StationDTO getStationByName(String name) {
        return findStationByName(name)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + name));
    }

    private Optional<StationDTO> findStationByName(String name)
    {
        ValidationUtil.validateNotBlank(name, "Name");

        return stationDAO.findByName(name)
            .map(this::mapToDTO);
    }

    private void requireChef(User user)
    {
        if (!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef or sous chef can manage stations");
        }
    }

    private void validateStationNameUnique(String name)
    {
        Optional<Station> existing = stationDAO.findByName(name);
        if (existing.isPresent())
        {
            throw new ValidationException("Station with name '" + name + "' already exists");
        }
    }

    private StationDTO mapToDTO(Station station)
    {
        return new StationDTO(
            station.getId(),
            station.getStationName(),
            station.getDescription()
        );
    }
}
