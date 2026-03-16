package app.persistence.daos.interfaces.readers;

import app.persistence.daos.interfaces.generic.IEntityReader;
import app.persistence.entities.Station;

import java.util.Optional;

public interface IStationReader extends IEntityReader<Station, Long>
{
    Optional<Station> findByName(String name);
}
