package app.persistence.daos.impl;

import app.enums.ShoppingListStatus;
import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IShoppingListDAO;
import app.persistence.entities.ShoppingList;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ShoppingListDAO implements IShoppingListDAO
{
    private final EntityManagerFactory emf;

    public ShoppingListDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }


    @Override
    public ShoppingList create(ShoppingList shoppingList)
    {
        ValidationUtil.validateNotNull(shoppingList, "ShoppingList");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(shoppingList);
                em.getTransaction().commit();
                return getByID(shoppingList.getId());
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create shopping list", e);
            }
        }
    }

    @Override
    public ShoppingList getByID(Long id)
    {
        ValidationUtil.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                ShoppingList list = em.createQuery(
                        "SELECT sl FROM ShoppingList sl " +
                            "LEFT JOIN FETCH sl.shoppingListItems " +
                            "WHERE sl.id = :id",
                        ShoppingList.class)
                    .setParameter("id", id)
                    .getSingleResult();

                return DBValidator.validateExists(list, id, ShoppingList.class);
            }
            catch (NoResultException e)
            {
                throw new EntityNotFoundException("ShoppingList with id " + id + " not found");
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch shopping list: " + id, e);
            }
        }
    }

    @Override
    public ShoppingList update(ShoppingList shoppingList)
    {
        ValidationUtil.validateNotNull(shoppingList, "ShoppingList");
        ValidationUtil.validateId(shoppingList.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();

                ShoppingList existing = em.find(ShoppingList.class, shoppingList.getId());
                DBValidator.validateExists(existing, shoppingList.getId(), ShoppingList.class);
                ShoppingList merged = em.merge(shoppingList);
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
                throw new DatabaseException("Failed to update shopping list: " + shoppingList.getId(), e);
            }
        }
    }

    @Override
    public Optional<ShoppingList> findByDeliveryDate(LocalDate deliveryDate)
    {
        ValidationUtil.validateNotNull(deliveryDate, "DeliveryDate");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                ShoppingList list = em.createQuery(
                        "SELECT sl FROM ShoppingList sl " +
                            "LEFT JOIN FETCH sl.shoppingListItems " +
                            "WHERE sl.deliveryDate = :date",
                        ShoppingList.class)
                    .setParameter("date", deliveryDate)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                return Optional.ofNullable(list);
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch shopping list for date: " + deliveryDate, e);
            }
        }
    }

    @Override
    public List<ShoppingList> findByFilter(ShoppingListStatus status, LocalDate deliveryDate)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                StringBuilder jpql = new StringBuilder(
                    """
                        SELECT sl FROM ShoppingList sl
                        LEFT JOIN FETCH sl.shoppingListItems
                        WHERE 1=1
                    """
                );

                if (status != null) jpql.append("AND sl.shoppingListStatus = :status ");
                if (deliveryDate != null) jpql.append("AND sl.deliveryDate = :deliveryDate ");

                jpql.append("ORDER BY sl.deliveryDate ASC");

                TypedQuery<ShoppingList> query = em.createQuery(jpql.toString(), ShoppingList.class);

                if (status != null) query.setParameter("status", status);
                if (deliveryDate != null) query.setParameter("deliveryDate", deliveryDate);

                return query.getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch shopping lists", e);
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

                ShoppingList managed = em.find(ShoppingList.class, id);
                DBValidator.validateExists(managed, id, ShoppingList.class);

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
                throw new DatabaseException("Failed to delete shopping list: " + id, e);
            }
        }
    }
}
