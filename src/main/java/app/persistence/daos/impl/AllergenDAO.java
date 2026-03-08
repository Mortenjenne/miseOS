package app.persistence.daos.impl;

import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.entities.Allergen;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class AllergenDAO implements IAllergenDAO
{
    private final EntityManagerFactory emf;

    public AllergenDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Optional<Allergen> findByNameDA(String nameDA)
    {
        DBValidator.validateNotBlank(nameDA, "Name DA");

        try(EntityManager em = emf.createEntityManager())
        {
            Allergen allergen = em.createQuery("SELECT a FROM Allergen a WHERE a.nameDA ILIKE :nameDA", Allergen.class)
                .setParameter("nameDA","%" + nameDA + "%")
                .getResultStream()
                .findFirst()
                .orElse(null);

            return Optional.ofNullable(allergen);
        }
    }

    @Override
    public Optional<Allergen> findByNameEN(String nameEN)
    {
        DBValidator.validateNotBlank(nameEN, "Name EN");

        try(EntityManager em = emf.createEntityManager())
        {
            Allergen allergen = em.createQuery("SELECT a FROM Allergen a WHERE a.nameEN ILIKE :nameEN", Allergen.class)
                .setParameter("nameEN","%" + nameEN + "%")
                .getResultStream()
                .findFirst()
                .orElse(null);

            return Optional.ofNullable(allergen);
        }
    }

    @Override
    public long count()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("SELECT COUNT(a) FROM Allergen a", Long.class)
                .getSingleResult();
        }
    }

    @Override
    public boolean isUsedByAnyDish(Long allergenId)
    {
        DBValidator.validateId(allergenId);

        try (EntityManager em = emf.createEntityManager())
        {
            Long dishCount = em.createQuery(
                    "SELECT COUNT(d) FROM Dish d JOIN d.allergens a WHERE a.id = :allergenId",
                    Long.class
                )
                .setParameter("allergenId", allergenId)
                .getSingleResult();

            Long suggestionCount = em.createQuery(
                    "SELECT COUNT(s) FROM DishSuggestion s JOIN s.allergens a WHERE a.id = :allergenId",
                    Long.class
                )
                .setParameter("allergenId", allergenId)
                .getSingleResult();

            return (dishCount + suggestionCount) > 0;
        }
    }

    @Override
    public boolean existsByDisplayNumber(Integer displayNumber)
    {
        DBValidator.validatePositive(displayNumber, "Display number");

        try(EntityManager em = emf.createEntityManager())
        {
            Long count = em.createQuery("SELECT COUNT(a) FROM Allergen a WHERE a.displayNumber = :displayNumber", Long.class)
                .setParameter("displayNumber", displayNumber)
                .getSingleResult();

            return count > 0;
        }
    }

    @Override
    public Allergen create(Allergen allergen)
    {
        DBValidator.validateNotNull(allergen, "Allergen");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(allergen);
                em.getTransaction().commit();
                return allergen;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create Allergen", e);
            }
        }
    }

    @Override
    public Set<Allergen> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Allergen> query = em.createQuery("SELECT a FROM Allergen a ORDER BY a.displayNumber ASC", Allergen.class);
            return new LinkedHashSet<>(query.getResultList());
        }
    }

    @Override
    public Allergen getByID(Long id)
    {
        DBValidator.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            Allergen allergen = em.find(Allergen.class, id);
            return DBValidator.validateExists(allergen, id, Allergen.class);
        }
    }

    @Override
    public Allergen update(Allergen allergen)
    {
        DBValidator.validateNotNull(allergen, "User");
        DBValidator.validateId(allergen.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                Allergen exist = em.find(Allergen.class, allergen.getId());
                DBValidator.validateExists(exist, allergen.getId(), Allergen.class);
                Allergen merged = em.merge(allergen);
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
                throw new DatabaseException("Failed to update Allergen: " + allergen.getId(), e);
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
                Allergen managed = em.find(Allergen.class, id);
                DBValidator.validateExists(managed, id, Allergen.class);
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
                throw new DatabaseException("Failed to delete Allergen: " + id, e);
            }
        }
    }
}
