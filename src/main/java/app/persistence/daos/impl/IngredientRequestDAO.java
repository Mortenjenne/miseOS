package app.persistence.daos.impl;

import app.enums.RequestType;
import app.enums.Status;
import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IIngredientRequestDAO;
import app.persistence.entities.IngredientRequest;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

public class IngredientRequestDAO implements IIngredientRequestDAO
{
    private final EntityManagerFactory emf;

    public IngredientRequestDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public List<IngredientRequest> findByFilter(Status status, LocalDate deliveryDate, Long creatorId, RequestType requestType, Long stationId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                StringBuilder jpql = new StringBuilder(
                    """
                    SELECT DISTINCT ir FROM IngredientRequest ir
                    LEFT JOIN FETCH ir.dish d
                    LEFT JOIN FETCH d.station st
                    LEFT JOIN FETCH ir.createdBy u
                    WHERE 1=1
                    """
                );

                if (status != null) jpql.append(" AND ir.requestStatus = :status");
                if (deliveryDate != null)jpql.append(" AND ir.deliveryDate = :deliveryDate");
                if (creatorId != null) jpql.append(" AND u.id = :creatorId");
                if (requestType != null) jpql.append(" AND ir.requestType = :requestType");
                if (stationId != null) jpql.append(" AND st.id = :stationId");

                jpql.append(" ORDER BY ir.name ASC, ir.createdAt ASC");

                TypedQuery<IngredientRequest> query = em.createQuery(jpql.toString(), IngredientRequest.class);

                if (status != null) query.setParameter("status", status);
                if (deliveryDate != null) query.setParameter("deliveryDate", deliveryDate);
                if (creatorId != null) query.setParameter("creatorId", creatorId);
                if (requestType != null) query.setParameter("requestType", requestType);
                if (stationId != null) query.setParameter("stationId", stationId);

                return query.getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch ingredient requests", e);
            }
        }
    }

    @Override
    public int getPendingRequestCount()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(ir) FROM IngredientRequest ir " +
                        "WHERE ir.requestStatus = :status",
                    Long.class
                );
                query.setParameter("status", Status.PENDING);

                return Math.toIntExact(query.getSingleResult());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to count all pending ingredient requests", e);
            }
        }
    }

    @Override
    public IngredientRequest create(IngredientRequest ingredientRequest)
    {
        ValidationUtil.validateNotNull(ingredientRequest, "IngredientRequest");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(ingredientRequest);
                em.getTransaction().commit();
                return ingredientRequest;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create IngredientRequest", e);
            }
        }
    }

    @Override
    public IngredientRequest getByID(Long id)
    {
        ValidationUtil.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                IngredientRequest ingredientRequest = em.find(IngredientRequest.class, id);
                return DBValidator.validateExists(ingredientRequest, id, IngredientRequest.class);
            }
            catch (EntityNotFoundException e)
            {
                throw e;
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Couldn't fetch ingredient request with id: " + id, e);
            }
        }
    }

    @Override
    public IngredientRequest update(IngredientRequest ingredientRequest)
    {
        ValidationUtil.validateNotNull(ingredientRequest, "IngredientRequest");
        ValidationUtil.validateId(ingredientRequest.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                IngredientRequest exist = em.find(IngredientRequest.class, ingredientRequest.getId());
                DBValidator.validateExists(exist, ingredientRequest.getId(), IngredientRequest.class);
                IngredientRequest merged = em.merge(ingredientRequest);
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
                throw new DatabaseException("Failed to update IngredientRequest: " + ingredientRequest.getId(), e);
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
                IngredientRequest managed = em.find(IngredientRequest.class, id);
                DBValidator.validateExists(managed, id, IngredientRequest.class);
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
                throw new DatabaseException("Failed to delete IngredientRequest: " + id, e);
            }
        }
    }
}
