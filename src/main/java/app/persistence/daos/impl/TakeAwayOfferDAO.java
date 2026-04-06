package app.persistence.daos.impl;

import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.ITakeAwayOfferDAO;
import app.persistence.entities.TakeAwayOffer;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

public class TakeAwayOfferDAO implements ITakeAwayOfferDAO
{
    private final EntityManagerFactory emf;

    public TakeAwayOfferDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<TakeAwayOffer> findByFilter(LocalDate date, Boolean isSoldOut, Boolean isEnabled, Long dishId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<TakeAwayOffer> query = em.createQuery(
                        "SELECT DISTINCT tao FROM TakeAwayOffer tao " +
                            "LEFT JOIN FETCH tao.dish d " +
                            "WHERE (:date IS NULL OR tao.createdAt = :date) " +
                            "AND   (:dishId IS NULL OR d.id = :dishId) " +
                            "AND   (:isEnabled IS NULL OR tao.enabled = :isEnabled) " +
                            "AND   (:isSoldOut IS NULL OR tao.soldOut = :isSoldOut) " +
                            "ORDER BY tao.createdAt DESC",
                        TakeAwayOffer.class)
                    .setParameter("date", date)
                    .setParameter("dishId", dishId)
                    .setParameter("isEnabled", isEnabled)
                    .setParameter("isSoldOut", isSoldOut);

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch active take away offers", e);
            }
        }
    }

    @Override
    public boolean existsByDishAndDate(Long dishId, LocalDate date)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateNotNull(date, "Date");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                Long count = em.createQuery(
                        "SELECT COUNT(to) FROM TakeAwayOffer to " +
                            "WHERE to.dish.id = :dishId " +
                            "AND to.createdAt = :date",
                        Long.class)
                    .setParameter("dishId", dishId)
                    .setParameter("date", date)
                    .getSingleResult();

                return count > 0;
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to check existing take away offer", e);
            }
        }
    }

    @Override
    public TakeAwayOffer getByID(Long id)
    {
        ValidationUtil.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery(
                        "SELECT to FROM TakeAwayOffer to " +
                            "JOIN FETCH to.dish d " +
                            "LEFT JOIN FETCH d.allergens " +
                            "JOIN FETCH to.createdBy " +
                            "WHERE to.id = :id",
                        TakeAwayOffer.class)
                    .setParameter("id", id)
                    .getSingleResult();
            }
            catch (NoResultException e)
            {
                throw new EntityNotFoundException("TakeAwayOffer with id: " + id + " not found");
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch take away offer by id", e);
            }
        }
    }

    @Override
    public TakeAwayOffer create(TakeAwayOffer offer)
    {
        ValidationUtil.validateNotNull(offer, "Take away offer");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(offer);
                em.getTransaction().commit();
                return getByID(offer.getId());
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create take away offer", e);
            }
        }
    }

    @Override
    public TakeAwayOffer update(TakeAwayOffer offer)
    {
        ValidationUtil.validateNotNull(offer, "Take away offer");
        ValidationUtil.validateId(offer.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();

                TakeAwayOffer existing = em.find(TakeAwayOffer.class, offer.getId());
                DBValidator.validateExists(existing, offer.getId(), TakeAwayOffer.class);

                TakeAwayOffer merged = em.merge(offer);
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
                throw new DatabaseException("Failed to update take away offer: " + offer.getId(), e);
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

                TakeAwayOffer managed = em.find(TakeAwayOffer.class, id);
                DBValidator.validateExists(managed, id, TakeAwayOffer.class);

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
                throw new DatabaseException("Failed to delete take away offer: " + id, e);
            }
        }
    }
}
