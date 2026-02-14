package app.persistence.daos;

import app.persistence.entities.Allergen;
import app.utils.DBValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
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
    public Optional<Allergen> findByName(String name)
    {
        try(EntityManager em = emf.createEntityManager())
        {
            Allergen allergen = em.createQuery("SELECT a FROM Allergen a WHERE a.name = :name", Allergen.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElse(null);

            return Optional.ofNullable(allergen);
        }
    }

    @Override
    public Allergen create(Allergen allergen)
    {
        DBValidator.validateNotNull(allergen, "Allergen");

        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(allergen);
            em.getTransaction().commit();
            return allergen;
        }
    }

    @Override
    public Set<Allergen> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Allergen> query = em.createQuery("SELECT a FROM Allergen a", Allergen.class);
            return new HashSet<>(query.getResultList());
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
                Allergen managed = em.find(Allergen.class, id);
                DBValidator.validateExists(managed, id, Allergen.class);
                em.remove(managed);
                em.getTransaction().commit();
                return true;

            } catch (RuntimeException e)
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
