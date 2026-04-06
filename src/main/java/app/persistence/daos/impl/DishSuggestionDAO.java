package app.persistence.daos.impl;

import app.enums.Status;
import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IDishSuggestionDAO;
import app.persistence.entities.DishSuggestion;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import app.utils.ValidationUtil;
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
    public Set<DishSuggestion> findByFilter(Status status, Long creatorId, Integer week, Integer year, Long stationId, String orderBy)
    {
        String target = orderBy != null ? orderBy : "";

        String orderColumn = switch (target)
        {
            case "status" -> "ds.dishStatus ASC";
            case "station" -> "ds.station.id ASC";
            case "createdAt" -> "ds.createdAt ASC";
            default -> "ds.targetYear ASC, ds.targetWeek ASC";
        };

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<DishSuggestion> query = em.createQuery(
                        "SELECT DISTINCT ds FROM DishSuggestion ds " +
                            "LEFT JOIN FETCH ds.allergens " +
                            "WHERE (:status IS NULL OR ds.dishStatus = :status) " +
                            "AND (:creatorId IS NULL OR ds.createdBy.id = :creatorId) " +
                            "AND (:week IS NULL OR ds.targetWeek = :week) " +
                            "AND (:year IS NULL OR ds.targetYear = :year) " +
                            "AND (:stationId IS NULL OR ds.station.id = :stationId) " +
                            "ORDER BY " + orderColumn,
                        DishSuggestion.class)
                    .setParameter("status", status)
                    .setParameter("creatorId", creatorId)
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
    public int getPendingSuggestionsCount()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(ds) FROM DishSuggestion ds " +
                        "WHERE ds.dishStatus = :status",
                    Long.class
                );
                query.setParameter("status", Status.PENDING);

                return Math.toIntExact(query.getSingleResult());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to count all pending dishes", e);
            }
        }
    }

    @Override
    public DishSuggestion getByID(Long id)
    {
        ValidationUtil.validateId(id);

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
        ValidationUtil.validateNotNull(dishSuggestion, "DishSuggestion");

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
    public DishSuggestion update(DishSuggestion dishSuggestion)
    {
        ValidationUtil.validateNotNull(dishSuggestion, "DishSuggestion");
        ValidationUtil.validateId(dishSuggestion.getId());

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
        ValidationUtil.validateId(id);

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
