package app.persistence.daos.impl;

import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IStationDAO;
import app.persistence.entities.Station;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class StationDAO implements IStationDAO
{
    private final EntityManagerFactory emf;

    public StationDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Station create(Station station)
    {
        ValidationUtil.validateNotNull(station, "Station");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(station);
                em.getTransaction().commit();
                return station;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create station", e);
            }
        }
    }

    @Override
    public Set<Station> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Station> query = em.createQuery("SELECT s FROM Station s ORDER BY s.stationName ASC", Station.class);
                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch all stations", e);
            }
        }
    }

    @Override
    public Station getByID(Long id)
    {
        ValidationUtil.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                Station station = em.find(Station.class, id);
                return DBValidator.validateExists(station, id, Station.class);
            }
            catch (EntityNotFoundException e)
            {
                throw e;
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch station by id: " + id, e);
            }
        }
    }

    @Override
    public Station update(Station station)
    {
        ValidationUtil.validateNotNull(station, "Station");
        ValidationUtil.validateId(station.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                Station exist = em.find(Station.class, station.getId());
                DBValidator.validateExists(exist, station.getId(), Station.class);
                Station merged = em.merge(station);
                em.getTransaction().commit();
                return merged;
            }
            catch (EntityNotFoundException e)
            {
                TransactionUtil.rollback(em);
                throw e;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to update station: " + station.getId(), e);
            }
        }
    }

    @Override
    public boolean delete(Long id)
    {
        ValidationUtil.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                Station managed = em.find(Station.class, id);
                DBValidator.validateExists(managed, id, Station.class);
                em.remove(managed);
                em.getTransaction().commit();
                return true;
            }
            catch (EntityNotFoundException e)
            {
                TransactionUtil.rollback(em);
                throw e;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to delete station: " + id, e);
            }
        }
    }

    @Override
    public Optional<Station> findByName(String name)
    {
        ValidationUtil.validateNotNull(name, "Name");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                Station station = em.createQuery("SELECT st FROM Station st WHERE st.stationName ILIKE :name", Station.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                return Optional.ofNullable(station);
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to find station by name: " + name, e);
            }
        }
    }

    @Override
    public boolean isUsedByAnyDish(Long stationId)
    {
        ValidationUtil.validateId(stationId);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                Long dishCount = em.createQuery(
                        "SELECT COUNT(d) FROM Dish d JOIN d.station s WHERE s.id = :stationId",
                        Long.class
                    )
                    .setParameter("stationId", stationId)
                    .getSingleResult();

                Long suggestionCount = em.createQuery(
                        "SELECT COUNT(s) FROM DishSuggestion d JOIN d.station s WHERE s.id = :stationId",
                        Long.class
                    )
                    .setParameter("stationId", stationId)
                    .getSingleResult();

                return (dishCount + suggestionCount) > 0;
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to check if station exists", e);
            }
        }
    }
}
