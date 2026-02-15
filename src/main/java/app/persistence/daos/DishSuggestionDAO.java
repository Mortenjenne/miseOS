package app.persistence.daos;

import app.enums.Status;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.User;
import app.utils.DBValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class DishSuggestionDAO implements IDishSuggestionDAO
{
    private final EntityManagerFactory emf;

    public DishSuggestionDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<DishSuggestion> findByStatus(Status status)
    {
        DBValidator.validateNotNull(status, "Status");

        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<DishSuggestion> query = em.createQuery(
                    "SELECT d FROM DishSuggestion d WHERE d.dishStatus = :status",
                    DishSuggestion.class)
                .setParameter("status", status);

            return new HashSet<>(query.getResultList());
        }
    }

    public Set<DishSuggestion> findByWeekAndYear(int weekNumber, int year)
    {
        DBValidator.validateRange(weekNumber, 1, 53, "Week number");
        DBValidator.validateRange(year, 2000, 2100, "Year");

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<DishSuggestion> query = em.createQuery(
                "SELECT DISTINCT wms.dishSuggestion " +
                    "FROM WeeklyMenuSlot wms " +
                    "WHERE wms.weeklyMenu.weekNumber = :weekNumber " +
                    "AND wms.weeklyMenu.year = :year " +
                    "AND wms.dishSuggestion IS NOT NULL",
                DishSuggestion.class
            );

            query.setParameter("weekNumber", weekNumber);
            query.setParameter("year", year);

            return new LinkedHashSet<>(query.getResultList());
        }
    }

    @Override
    public Set<DishSuggestion> findByStationAndStatus(Long stationId, Status status)
    {
        DBValidator.validateId(stationId);
        DBValidator.validateNotNull(status, "Status");

        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<DishSuggestion> query = em.createQuery(
                    "SELECT d FROM DishSuggestion d WHERE d.station.id = :stationId AND d.dishStatus = :status",
                    DishSuggestion.class)
                .setParameter("stationId", stationId)
                .setParameter("status", status);

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Optional<DishSuggestion> getByIdWithAllergens(Long id)
    {
        DBValidator.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                DishSuggestion dish = em.createQuery(
                        "SELECT d FROM DishSuggestion d LEFT JOIN FETCH d.allergens WHERE d.id = :id",
                        DishSuggestion.class)
                    .setParameter("id", id)
                    .getSingleResult();

                return Optional.of(dish);
            }
            catch (NoResultException e)
            {
                return Optional.empty();
            }
        }
    }

    @Override
    public DishSuggestion create(DishSuggestion dishSuggestion)
    {
        DBValidator.validateNotNull(dishSuggestion, "DishSuggestion");

        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(dishSuggestion);
            em.getTransaction().commit();
            return dishSuggestion;
        }
    }

    @Override
    public Set<DishSuggestion> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<DishSuggestion> query = em.createQuery("SELECT d FROM DishSuggestion d", DishSuggestion.class);
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public DishSuggestion getByID(Long id)
    {
        DBValidator.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            DishSuggestion dishSuggestion = em.find(DishSuggestion.class, id);
            return DBValidator.validateExists(dishSuggestion, id, DishSuggestion.class);
        }
    }

    @Override
    public DishSuggestion update(DishSuggestion dishSuggestion)
    {
        DBValidator.validateNotNull(dishSuggestion, "DishSuggestion");
        DBValidator.validateId(dishSuggestion.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                DishSuggestion exist = em.find(DishSuggestion.class, dishSuggestion.getId());
                DBValidator.validateExists(exist, dishSuggestion.getId(), DishSuggestion.class);
                DishSuggestion merged = em.merge(dishSuggestion);
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
                DishSuggestion managed = em.find(DishSuggestion.class, id);
                DBValidator.validateExists(managed, id, DishSuggestion.class);
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
}
