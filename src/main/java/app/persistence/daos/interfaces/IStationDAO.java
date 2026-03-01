package app.persistence.daos.interfaces;

import app.persistence.entities.Station;

public interface IStationDAO extends IStationReader, IEntityDAO<Station, Long>
{}
