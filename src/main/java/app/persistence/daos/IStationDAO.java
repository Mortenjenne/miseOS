package app.persistence.daos;

import app.persistence.entities.Station;

import java.util.Optional;

public interface IStationDAO extends IEntityDAO<Station, Long>
{
    Optional<Station> findByName(String name);
}
