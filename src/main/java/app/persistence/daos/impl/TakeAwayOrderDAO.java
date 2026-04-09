package app.persistence.daos.impl;

import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.ITakeAwayOrderDAO;
import app.persistence.entities.TakeAwayOrder;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class TakeAwayOrderDAO implements ITakeAwayOrderDAO
{
    private final EntityManagerFactory emf;

    public TakeAwayOrderDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<TakeAwayOrder> findByOfferId(Long offerId)
    {
        ValidationUtil.validateId(offerId);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<TakeAwayOrder> query = em.createQuery(
                        "SELECT DISTINCT ord FROM TakeAwayOrder ord " +
                            "JOIN FETCH ord.customer " +
                            "JOIN FETCH ord.orderLines lines " +
                            "JOIN FETCH lines.takeAwayOffer off " +
                            "WHERE off.id = :offerId " +
                            "ORDER BY ord.orderedAt DESC",
                        TakeAwayOrder.class)
                    .setParameter("offerId", offerId);

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch take away orders by offer", e);
            }
        }
    }

    @Override
    public Optional<Long> sumSoldQuantityByDate(LocalDate date)
    {
        ValidationUtil.validateNotNull(date, "Date");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                Long total = em.createQuery(
                        "SELECT SUM(lines.quantity) " +
                            "FROM TakeAwayOrder ord " +
                            "JOIN ord.orderLines lines " +
                            "WHERE ord.createdAt = :date",
                        Long.class)
                    .setParameter("date", date)
                    .getSingleResult();

                return Optional.ofNullable(total);
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to sum sold takeaway quantity by date", e);
            }
        }
    }

    @Override
    public Optional<Long> countOrdersByDate(LocalDate date)
    {
        ValidationUtil.validateNotNull(date, "Date");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                Long total = em.createQuery(
                        "SELECT COUNT(ord) " +
                            "FROM TakeAwayOrder ord " +
                            "WHERE ord.createdAt = :date",
                        Long.class)
                    .setParameter("date", date)
                    .getSingleResult();

                return Optional.ofNullable(total);
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to count orders by date", e);
            }
        }
    }

    @Override
    public Set<TakeAwayOrder> findByDate(LocalDate date)
    {
        ValidationUtil.validateNotNull(date, "Date");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<TakeAwayOrder> query = em.createQuery(
                        "SELECT DISTINCT ord FROM TakeAwayOrder ord " +
                            "JOIN FETCH ord.customer " +
                            "LEFT JOIN FETCH ord.orderLines lines " +
                            "LEFT JOIN FETCH lines.takeAwayOffer off " +
                            "LEFT JOIN FETCH off.dish " +
                            "WHERE ord.createdAt = :date " +
                            "ORDER BY ord.orderedAt DESC",
                        TakeAwayOrder.class)
                    .setParameter("date", date);

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch take away orders by date", e);
            }
        }
    }

    @Override
    public TakeAwayOrder getByID(Long id)
    {
        ValidationUtil.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery(
                        "SELECT ord FROM TakeAwayOrder ord " +
                            "JOIN FETCH ord.customer " +
                            "LEFT JOIN FETCH ord.orderLines lines " +
                            "LEFT JOIN FETCH lines.takeAwayOffer off " +
                            "LEFT JOIN FETCH off.dish " +
                            "WHERE ord.id = :id",
                        TakeAwayOrder.class)
                    .setParameter("id", id)
                    .getSingleResult();
            }
            catch (NoResultException e)
            {
                throw new EntityNotFoundException("TakeAwayOrder with id: " + id + " not found");
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch take away order by id", e);
            }
        }
    }

    @Override
    public TakeAwayOrder create(TakeAwayOrder order)
    {
        ValidationUtil.validateNotNull(order, "Take away order");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(order);
                em.getTransaction().commit();
                return getByID(order.getId());
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create take away order", e);
            }
        }
    }

    @Override
    public TakeAwayOrder update(TakeAwayOrder order)
    {
        ValidationUtil.validateNotNull(order, "Take away order");
        ValidationUtil.validateId(order.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();

                TakeAwayOrder existing = em.find(TakeAwayOrder.class, order.getId());
                DBValidator.validateExists(existing, order.getId(), TakeAwayOrder.class);

                TakeAwayOrder merged = em.merge(order);
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
                throw new DatabaseException("Failed to update take away order: " + order.getId(), e);
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

                TakeAwayOrder managed = em.find(TakeAwayOrder.class, id);
                DBValidator.validateExists(managed, id, TakeAwayOrder.class);

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
                throw new DatabaseException("Failed to delete take away order: " + id, e);
            }
        }
    }
}
