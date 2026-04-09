package app.persistence.daos.impl;

import app.dtos.takeaway.TakeAwayOrderCreateDTO;
import app.dtos.takeaway.TakeAwayOrderLineCreateDTO;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.ITakeAwayOrderDAO;
import app.persistence.entities.TakeAwayOffer;
import app.persistence.entities.TakeAwayOrder;
import app.persistence.entities.TakeAwayOrderLine;
import app.persistence.entities.User;
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
    public Set<TakeAwayOrder> findByFilter(Long customerId, Long offerId, LocalDate date, OrderStatus status)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                StringBuilder jpql = new StringBuilder(
                    "SELECT DISTINCT ord FROM TakeAwayOrder ord " +
                        "JOIN FETCH ord.customer c " +
                        "LEFT JOIN FETCH ord.orderLines lines " +
                        "LEFT JOIN FETCH lines.takeAwayOffer off " +
                        "WHERE 1=1"
                );

                if (customerId != null) jpql.append(" AND c.id = :customerId");
                if (offerId != null)    jpql.append(" AND off.id = :offerId");
                if (date != null)       jpql.append(" AND ord.createdAt = :date");
                if (status != null)     jpql.append(" AND ord.orderStatus = :status");

                jpql.append(" ORDER BY ord.orderedAt DESC");

                TypedQuery<TakeAwayOrder> query = em.createQuery(jpql.toString(), TakeAwayOrder.class);

                if (customerId != null) query.setParameter("customerId", customerId);
                if (offerId != null)    query.setParameter("offerId", offerId);
                if (date != null)       query.setParameter("date", date);
                if (status != null)     query.setParameter("status", status);

                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch take away orders with filter", e);
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
    public TakeAwayOrder create(Long customerId, TakeAwayOrderCreateDTO dto)
    {
        ValidationUtil.validateId(customerId);
        ValidationUtil.validateNotNull(dto, "Takeaway Order Create");
        ValidationUtil.validateNotEmpty(dto.takeAwayOrderLines(), "Order Lines");

        Long orderId;

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();

                User customer = em.find(User.class, customerId);
                DBValidator.validateExists(customer, customerId, User.class);

                TakeAwayOrder order = new TakeAwayOrder(customer);

                for (TakeAwayOrderLineCreateDTO lineDTO : dto.takeAwayOrderLines())
                {
                    TakeAwayOffer offer = em.find(TakeAwayOffer.class, lineDTO.offerId());
                    DBValidator.validateExists(offer, lineDTO.offerId(), TakeAwayOffer.class);

                    offer.sellPortions(lineDTO.quantity());

                    TakeAwayOrderLine line = new TakeAwayOrderLine(order, offer, lineDTO.quantity());
                    order.addOrderLine(line);
                }

                em.persist(order);
                em.getTransaction().commit();

                orderId = order.getId();
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to place takeaway order", e);
            }
            catch (RuntimeException e)
            {
                TransactionUtil.rollback(em);
                throw e;
            }
        }

        return getByID(orderId);
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
