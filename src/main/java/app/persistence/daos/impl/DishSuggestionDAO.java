package app.persistence.daos.impl;

import app.enums.Status;
import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IDishSuggestionDAO;
import app.persistence.entities.DishSuggestion;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class DishSuggestionDAO implements IDishSuggestionDAO
{
    private final EntityManagerFactory emf;

    public DishSuggestionDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<DishSuggestion> findByFilter(Status status, Integer week, Integer year, Long stationId, String orderBy)
    {
        String target = orderBy != null ? orderBy : "";
        String orderColumn = switch (target)
        {
            case "status" -> "ds.dishStatus";
            case "station" -> "ds.station.stationName";
            case "createdAt" -> "ds.createdAt ASC";
            default -> "ds.targetYear ASC, ds.targetWeek";
        };

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<DishSuggestion> query = em.createQuery(
                        "SELECT DISTINCT ds FROM DishSuggestion ds " +
                            "LEFT JOIN FETCH ds.allergens " +
                            "WHERE (:status IS NULL OR ds.dishStatus  = :status) " +
                            "AND (:week IS NULL OR ds.targetWeek  = :week) " +
                            "AND (:year IS NULL OR ds.targetYear  = :year) " +
                            "AND (:stationId IS NULL OR ds.station.id = :stationId) " +
                            "ORDER BY " + orderColumn,
                        DishSuggestion.class)
                    .setParameter("status", status)
                    .setParameter("week", week)
                    .setParameter("year", year)
                    .setParameter("stationId", stationId);

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch dish suggestions", e);
            }
        }
    }

    @Override
    public DishSuggestion getByID(Long id)
    {
        DBValidator.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery(
                        "SELECT d FROM DishSuggestion d LEFT JOIN FETCH d.allergens WHERE d.id = :id",
                        DishSuggestion.class)
                    .setParameter("id", id)
                    .getSingleResult();

            }
            catch (NoResultException e)
            {
                throw new EntityNotFoundException("Dish with id: " + id + " not found");
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch dish by id", e);
            }
        }
    }

    @Override
    public DishSuggestion create(DishSuggestion dishSuggestion)
    {
        DBValidator.validateNotNull(dishSuggestion, "DishSuggestion");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
            em.getTransaction().begin();
            em.persist(dishSuggestion);
            em.getTransaction().commit();
            return getByID(dishSuggestion.getId());
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create dish suggestion", e);
            }
        }
    }

    @Override
    public Set<DishSuggestion> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
            TypedQuery<DishSuggestion> query = em.createQuery("SELECT DISTINCT ds FROM DishSuggestion ds LEFT JOIN FETCH ds.allergens", DishSuggestion.class);
            return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to all dish suggestions", e);
            }
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
                throw new DatabaseException("Failed to update dish: " + dishSuggestion.getId(), e);
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

            } catch (EntityNotFoundException e)
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
