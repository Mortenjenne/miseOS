package app.persistence.daos.impl;

import app.enums.Status;
import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IIngredientRequestDAO;
import app.persistence.entities.IngredientRequest;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class IngredientRequestDAO implements IIngredientRequestDAO
{
    private final EntityManagerFactory emf;

    public IngredientRequestDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<IngredientRequest> findByStatus(Status status)
    {
        DBValidator.validateNotNull(status, "Status");

        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<IngredientRequest> query = em.createQuery("SELECT ir FROM IngredientRequest ir WHERE ir.requestStatus = :status", IngredientRequest.class)
                .setParameter("status", status);
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Set<IngredientRequest> findByStatusAndDeliveryDate(Status status, LocalDate deliveryDate)
    {
        DBValidator.validateNotNull(status, "Status");
        DBValidator.validateNotNull(deliveryDate, "Delivery date");

        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<IngredientRequest> query = em.createQuery("SELECT ir FROM IngredientRequest ir WHERE ir.requestStatus = :status AND ir.deliveryDate = :deliveryDate", IngredientRequest.class)
                .setParameter("status", status)
                .setParameter("deliveryDate", deliveryDate);
            return new HashSet<>(query.getResultList());
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
    public Set<IngredientRequest> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<IngredientRequest> query = em.createQuery("SELECT ir FROM IngredientRequest ir", IngredientRequest.class);
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public IngredientRequest getByID(Long id)
    {
        DBValidator.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            IngredientRequest ingredientRequest = em.find(IngredientRequest.class, id);
            return DBValidator.validateExists(ingredientRequest, id, IngredientRequest.class);
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
