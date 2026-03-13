package app.persistence.daos.impl;

import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IDishDAO;
import app.persistence.entities.Dish;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class DishDAO implements IDishDAO
{

    private final EntityManagerFactory emf;

    public DishDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<Dish> findByOriginWeekAndYear(int week, int year)
    {
        DBValidator.validateRange(week, 1, 53, "Week");
        DBValidator.validateRange(year, 2020, 2100, "Year");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Dish> query = em.createQuery(
                        "SELECT DISTINCT d FROM Dish d " +
                            "LEFT JOIN FETCH d.allergens " +
                            "WHERE d.originWeek = :week AND d.originYear = :year AND d.isActive = true " +
                            "ORDER BY d.nameDA", Dish.class)
                    .setParameter("week", week)
                    .setParameter("year", year);

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch dishes by origin week and year", e);
            }
        }
    }

    @Override
    public Set<Dish> findFromPreviousWeeks(int currentWeek, int currentYear)
    {
        DBValidator.validateRange(currentWeek, 1, 53, "Week");
        DBValidator.validateRange(currentYear, 2020, 2100, "Year");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Dish> query = em.createQuery(
                        "SELECT DISTINCT d FROM Dish d " +
                            "LEFT JOIN FETCH d.allergens" +
                            " WHERE d.isActive = true AND (d.originYear < :year OR (d.originYear = :year AND d.originWeek < :week)) " +
                            "ORDER BY d.nameDA ASC ", Dish.class)
                    .setParameter("week", currentWeek)
                    .setParameter("year", currentYear);

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch dishes from previous weeks", e);
            }
        }
    }

    @Override
    public Set<Dish> searchByName(String nameQuery)
    {
        DBValidator.validateNotBlank(nameQuery, "Search query");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Dish> query = em.createQuery(
                        "SELECT DISTINCT d FROM Dish d " +
                            "LEFT JOIN FETCH d.allergens " +
                            "WHERE d.isActive = true AND (LOWER(d.nameDA) LIKE LOWER(:query) OR LOWER(d.nameEN) LIKE LOWER(:query)) " +
                            "ORDER BY d.nameDA ASC", Dish.class
                    )
                    .setParameter("query", "%" + nameQuery.trim() + "%");

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to search dishes by name", e);
            }
        }
    }


    @Override
    public Dish create(Dish dish)
    {
        DBValidator.validateNotNull(dish, "Dish");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(dish);
                em.getTransaction().commit();
                return getByID(dish.getId());
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create dish", e);
            }
        }
    }

    @Override
    public Set<Dish> findByFilter(Long stationId, Boolean active)
    {
        if (stationId != null)
        {
            DBValidator.validateId(stationId);
        }

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Dish> query = em.createQuery(
                        "SELECT DISTINCT d FROM Dish d " +
                            "LEFT JOIN FETCH d.allergens " +
                            "WHERE (:stationId IS NULL OR d.station.id = :stationId) " +
                            "AND (:active IS NULL OR d.isActive = :active) " +
                            "ORDER BY d.nameDA ASC", Dish.class)
                    .setParameter("stationId", stationId)
                    .setParameter("active", active);

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch dishes by filter", e);
            }
        }
    }

    @Override
    public Dish getByID(Long id)
    {
        DBValidator.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery(
                        "SELECT d FROM Dish d " +
                            "LEFT JOIN FETCH d.allergens " +
                            "WHERE d.id = :id", Dish.class)
                    .setParameter("id", id)
                    .getSingleResult();
            } catch (NoResultException e)
            {
                throw new EntityNotFoundException("Dish with id: " + id + " not found");
            } catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch dish by id", e);
            }
        }
    }

    @Override
    public Dish update(Dish dish)
    {
        DBValidator.validateNotNull(dish, "Dish");
        DBValidator.validateId(dish.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();

                Dish existing = em.find(Dish.class, dish.getId());
                DBValidator.validateExists(existing, dish.getId(), Dish.class);

                Dish merged = em.merge(dish);
                em.getTransaction().commit();
                return getByID(merged.getId());
            }
            catch (EntityNotFoundException e)
            {
                TransactionUtil.rollback(em);
                throw e;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to update dish: " + dish.getId(), e);
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

                Dish dish = em.find(Dish.class, id);
                DBValidator.validateExists(dish, id, Dish.class);

                em.remove(dish);
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
                throw new DatabaseException("Failed to delete dish: " + id, e);
            }
        }
    }

    @Override
    public boolean isUsedInAnyMenu(Long dishId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                Long count = em.createQuery(
                        "SELECT COUNT(s) FROM WeeklyMenuSlot s " +
                            "WHERE s.dish.id = :dishId", Long.class)
                    .setParameter("dishId", dishId)
                    .getSingleResult();

                return count > 0;
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to check menu usage for dish: " + dishId, e);
            }
        }
    }
}
