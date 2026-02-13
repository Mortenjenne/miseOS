package app.persistence.daos;

import app.enums.MenuStatus;
import app.persistence.entities.WeeklyMenu;
import app.utils.DBValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class WeeklyMenuDAO implements IWeeklyMenuDAO
{
    private final EntityManagerFactory emf;

    public WeeklyMenuDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<WeeklyMenu> findByStatus(MenuStatus status)
    {
        DBValidator.validateNotNull(status, "Status");

        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<WeeklyMenu> query = em.createQuery("SELECT wm FROM WeeklyMenu wm WHERE wm.menuStatus = :status", WeeklyMenu.class)
                .setParameter("status", status);

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Optional<WeeklyMenu> findByWeekAndYear(int weekNumber, int year)
    {
        DBValidator.validateRange(weekNumber, 1, 53, "Week number");
        DBValidator.validateRange(year, 2000, 2100, "Year");

        try(EntityManager em = emf.createEntityManager())
        {
            WeeklyMenu weeklyMenu = em.createQuery("SELECT wm FROM WeeklyMenu wm WHERE wm.weekNumber = :weekNumber AND wm.year = :year", WeeklyMenu.class)
                .setParameter("weekNumber", weekNumber)
                .setParameter("year", year)
                .getResultStream()
                .findFirst()
                .orElse(null);

            return Optional.ofNullable(weeklyMenu);
        }
    }

    @Override
    public WeeklyMenu create(WeeklyMenu weeklyMenu)
    {
        DBValidator.validateNotNull(weeklyMenu, "WeeklyMenu");

        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(weeklyMenu);
            em.getTransaction().commit();
            return weeklyMenu;
        }
    }

    @Override
    public Set<WeeklyMenu> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<WeeklyMenu> query = em.createQuery("SELECT wm FROM WeeklyMenu wm", WeeklyMenu.class);
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public WeeklyMenu getByID(Long id)
    {
        DBValidator.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            WeeklyMenu weeklyMenu = em.find(WeeklyMenu.class, id);
            return DBValidator.validateExists(weeklyMenu, id, WeeklyMenu.class);
        }
    }

    @Override
    public WeeklyMenu update(WeeklyMenu weeklyMenu)
    {
        DBValidator.validateNotNull(weeklyMenu, "WeeklyMenu");
        DBValidator.validateId(weeklyMenu.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                WeeklyMenu exist = em.find(WeeklyMenu.class, weeklyMenu.getId());
                DBValidator.validateExists(exist, weeklyMenu.getId(), WeeklyMenu.class);
                WeeklyMenu merged = em.merge(weeklyMenu);
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
                WeeklyMenu managed = em.find(WeeklyMenu.class, id);
                DBValidator.validateExists(managed, id, WeeklyMenu.class);
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
