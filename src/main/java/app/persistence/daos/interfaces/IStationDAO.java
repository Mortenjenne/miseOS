package app.persistence.daos.interfaces;

import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.daos.interfaces.readers.IStationReader;
import app.persistence.entities.Station;

import java.util.Set;

public interface IStationDAO extends IStationReader, IEntityDAO<Station, Long>
{
    Set<Station> getAll();

    boolean isUsedByAnyDish(Long stationId);
}
