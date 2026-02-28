package app.persistence.daos.impl;

import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IDishDAO;
import app.persistence.entities.Dish;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DishDAO implements IDishDAO
{

    private final EntityManagerFactory emf;

    public DishDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<Dish> findAllActive()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Dish> query = em.createQuery("SELECT d FROM Dish d WHERE d.isActive = true", Dish.class);

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Set<Dish> findByStationAndActive(Long stationId)
    {
        DBValidator.validateId(stationId);

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Dish> query = em.createQuery("SELECT d FROM Dish d WHERE d.station.id = :stationId AND d.isActive = true", Dish.class)
                .setParameter("stationId", stationId);

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Set<Dish> findByOriginWeekAndYear(int week, int year)
    {
        DBValidator.validateRange(week, 1, 53, "Week");
        DBValidator.validateRange(year, 2020, 2100, "Year");

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Dish> query = em.createQuery(
                "SELECT d FROM Dish d WHERE d.originWeek = :week AND d.originYear = :year AND d.isActive = true", Dish.class)
                .setParameter("week", week)
                .setParameter("year", year);

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Set<Dish> findFromPreviousWeeks(int currentWeek, int currentYear)
    {
        DBValidator.validateRange(currentWeek, 1, 53, "Week");
        DBValidator.validateRange(currentYear, 2020, 2100, "Year");

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Dish> query = em.createQuery(
                "SELECT d FROM Dish d WHERE d.isActive = true AND (d.originYear < :year OR (d.originYear = :year AND d.originWeek < :week))", Dish.class)
                .setParameter("week", currentWeek)
                .setParameter("year", currentYear);

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Set<Dish> searchByName(String nameQuery)
    {
        DBValidator.validateNotBlank(nameQuery, "Search query");

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Dish> query = em.createQuery(
                "SELECT d FROM Dish d WHERE d.isActive = true AND (LOWER(d.nameDA) LIKE LOWER(:query) OR LOWER(d.nameEN) LIKE LOWER(:query))", Dish.class
                )
                .setParameter("query", "%" + nameQuery.trim() + "%");

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Optional<Dish> getByIdWithAllergens(Long id)
    {
        DBValidator.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                Dish dish = em.createQuery(
                    "SELECT d FROM Dish d LEFT JOIN FETCH d.allergens WHERE d.id = :id", Dish.class
                    )
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
                return dish;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create dish", e);
            }
        }
    }

    @Override
    public Set<Dish> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Dish> query = em.createQuery("SELECT d FROM Dish d", Dish.class);

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Dish getByID(Long id)
    {
        DBValidator.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            Dish dish = em.find(Dish.class, id);
            return DBValidator.validateExists(dish, id, Dish.class);
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
}
