package app.persistence.daos;

import app.persistence.entities.Station;
import app.utils.DBValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
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
        DBValidator.validateNotNull(station, "Station");

        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(station);
            em.getTransaction().commit();
            return station;
        }
    }

    @Override
    public Set<Station> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Station> query = em.createQuery("SELECT s FROM Station s", Station.class);
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Station getByID(Long id)
    {
        DBValidator.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            Station station = em.find(Station.class, id);
            return DBValidator.validateExists(station, id, Station.class);
        }
    }

    @Override
    public Station update(Station station)
    {
        DBValidator.validateNotNull(station, "Station");
        DBValidator.validateId(station.getId());

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
            catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }

    @Override
    public boolean delete(Long id)
    {
        DBValidator.validateId(id);

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

            } catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }

    @Override
    public Optional<Station> findByName(String name)
    {
        DBValidator.validateNotNull(name, "Name");

        try(EntityManager em = emf.createEntityManager())
        {
            Station station = em.createQuery("SELECT st FROM Station st WHERE st.stationName = :name", Station.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElse(null);

            return Optional.ofNullable(station);
        }
    }
}
