package app.persistence.daos.interfaces;

import app.persistence.entities.Station;

import java.util.Optional;

public interface IStationReader extends IEntityReader<Station, Long>
{
    Optional<Station> findByName(String name);
}
