package app.persistence.daos.impl;

import app.enums.RequestType;
import app.enums.Status;
import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IIngredientRequestDAO;
import app.persistence.entities.IngredientRequest;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
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
    public List<IngredientRequest> findByFilter(Status status, LocalDate deliveryDate, Long creatorId, RequestType requestType)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<IngredientRequest> query = em.createQuery(
                        "SELECT DISTINCT ir FROM IngredientRequest ir " +
                            "LEFT JOIN ir.dish d " +
                            "LEFT JOIN ir.createdBy u " +
                            "WHERE (:status IS NULL OR ir.requestStatus  = :status) " +
                            "AND (:deliverDate IS NULL OR ir.deliveryDate  = :deliverDate) " +
                            "AND   (:creatorId IS NULL OR u.id = :creatorId) " +
                            "AND   (:requestType IS NULL OR ir.requestType = :requestType) " +
                            "ORDER BY ir.name ASC, ir.createdAt ASC",
                        IngredientRequest.class)
                    .setParameter("status", status)
                    .setParameter("deliverDate", deliveryDate)
                    .setParameter("creatorId", creatorId)
                    .setParameter("requestType", requestType);

                return query.getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch dish suggestions", e);
            }
        }
    }

    @Override
    public IngredientRequest create(IngredientRequest ingredientRequest)
    {
        DBValidator.validateNotNull(ingredientRequest, "IngredientRequest");

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
        DBValidator.validateId(id);

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
        DBValidator.validateNotNull(ingredientRequest, "IngredientRequest");
        DBValidator.validateId(ingredientRequest.getId());

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
        DBValidator.validateId(id);

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
