package app.persistence.daos;

import app.persistence.entities.Station;

public interface IStationDAO extends IStationReader, IEntityDAO<Station, Long>
{}
