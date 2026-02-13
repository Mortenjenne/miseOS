package app.persistence.daos;

import app.enums.ShoppingListStatus;
import app.persistence.entities.ShoppingList;
import app.utils.DBValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ShoppingListDAO implements IShoppingListDAO
{
    private final EntityManagerFactory emf;

    public ShoppingListDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Set<ShoppingList> findByStatus(ShoppingListStatus status)
    {
        DBValidator.validateNotNull(status, "ShoppingListStatus");

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<ShoppingList> query = em.createQuery(
                    "SELECT sl FROM ShoppingList sl WHERE sl.shoppingListStatus = :status",
                    ShoppingList.class)
                .setParameter("status", status);

            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public Optional<ShoppingList> findByDeliveryDate(LocalDate deliveryDate)
    {
        DBValidator.validateNotNull(deliveryDate, "DeliveryDate");

        try (EntityManager em = emf.createEntityManager())
        {
                ShoppingList list = em.createQuery(
                        "SELECT sl FROM ShoppingList sl WHERE sl.deliveryDate = :date",
                        ShoppingList.class)
                    .setParameter("date", deliveryDate)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                return Optional.ofNullable(list);
        }
    }

    @Override
    public ShoppingList create(ShoppingList shoppingList)
    {
        DBValidator.validateNotNull(shoppingList, "ShoppingList");

        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(shoppingList);
            em.getTransaction().commit();
            return shoppingList;
        }
    }

    @Override
    public Set<ShoppingList> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<ShoppingList> query = em.createQuery(
                "SELECT sl FROM ShoppingList sl", ShoppingList.class);
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public ShoppingList getByID(Long id)
    {
        DBValidator.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            ShoppingList list = em.find(ShoppingList.class, id);
            return DBValidator.validateExists(list, id, ShoppingList.class);
        }
    }

    @Override
    public ShoppingList update(ShoppingList shoppingList)
    {
        DBValidator.validateNotNull(shoppingList, "ShoppingList");
        DBValidator.validateId(shoppingList.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();

                ShoppingList existing = em.find(ShoppingList.class, shoppingList.getId());
                DBValidator.validateExists(existing, shoppingList.getId(), ShoppingList.class);
                ShoppingList merged = em.merge(shoppingList);
                em.getTransaction().commit();
                return merged;

            }
            catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw e;
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

                ShoppingList managed = em.find(ShoppingList.class, id);
                DBValidator.validateExists(managed, id, ShoppingList.class);

                em.remove(managed);  // Cascades delete to items
                em.getTransaction().commit();
                return true;

            }
            catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }
}
