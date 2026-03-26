package app.persistence.daos.impl;

import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.entities.Allergen;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.List;
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
    public List<Allergen> searchByName(String query)
    {
        ValidationUtil.validateNotBlank(query, "Query");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery(
                        "SELECT a FROM Allergen a" +
                            " WHERE a.nameDA ILIKE :nameDA OR a.nameEN ILIKE :query",
                        Allergen.class)
                    .setParameter("query", "%" + query + "%")
                    .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to search allergens by name: " + query, e);
            }
        }
    }

    @Override
    public Optional<Allergen> findByNameDA(String nameDA)
    {
        ValidationUtil.validateNotBlank(nameDA, "Name DA");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                Allergen allergen = em.createQuery("SELECT a FROM Allergen a" +
                        " WHERE a.nameDA ILIKE :nameDA", Allergen.class)
                    .setParameter("nameDA", "%" + nameDA + "%")
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                return Optional.ofNullable(allergen);
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch allergen with name: "+ nameDA, e);
            }
        }
    }

    @Override
    public Optional<Allergen> findByNameEN(String nameEN)
    {
        ValidationUtil.validateNotBlank(nameEN, "Name EN");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                Allergen allergen = em.createQuery("SELECT a FROM Allergen a WHERE a.nameEN ILIKE :nameEN", Allergen.class)
                    .setParameter("nameEN", "%" + nameEN + "%")
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                return Optional.ofNullable(allergen);
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch allergen with name: "+ nameEN, e);
            }
        }
    }

    @Override
    public long count()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery("SELECT COUNT(a) FROM Allergen a", Long.class)
                    .getSingleResult();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to count allergens", e);
            }
        }
    }

    @Override
    public boolean isUsedByAnyDish(Long allergenId)
    {
        ValidationUtil.validateId(allergenId);

        try (EntityManager em = emf.createEntityManager())
        {
            try
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
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to check if allergen is used in any dish", e);
            }
        }
    }

    @Override
    public boolean existsByDisplayNumber(Integer displayNumber)
    {
        ValidationUtil.validatePositive(displayNumber, "Display number");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                Long count = em.createQuery("SELECT COUNT(a) FROM Allergen a WHERE a.displayNumber = :displayNumber", Long.class)
                    .setParameter("displayNumber", displayNumber)
                    .getSingleResult();

                return count > 0;
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to read existing allergens", e);
            }
        }
    }

    @Override
    public Allergen create(Allergen allergen)
    {
        ValidationUtil.validateNotNull(allergen, "Allergen");

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
            try
            {
                TypedQuery<Allergen> query = em.createQuery("SELECT a FROM Allergen a ORDER BY a.displayNumber ASC", Allergen.class);
                return new LinkedHashSet<>(query.getResultList());
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch all allergens", e);
            }
        }
    }

    @Override
    public Allergen getByID(Long id)
    {
        ValidationUtil.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                Allergen allergen = em.find(Allergen.class, id);
                return DBValidator.validateExists(allergen, id, Allergen.class);
            }
            catch (EntityNotFoundException e)
            {
                throw e;
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch allergen with id: " + id, e);
            }
        }
    }

    @Override
    public Allergen update(Allergen allergen)
    {
        ValidationUtil.validateNotNull(allergen, "User");
        ValidationUtil.validateId(allergen.getId());

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
        ValidationUtil.validateId(id);

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
