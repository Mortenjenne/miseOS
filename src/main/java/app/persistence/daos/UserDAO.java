package app.persistence.daos;

import app.enums.UserRole;
import app.persistence.entities.User;
import app.utils.DBValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDAO implements IEntityDAO<User, Long>, IUserDAO
{
    private final EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public User create(User user)
    {
        DBValidator.validateNotNull(user, "User");

        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public Set<User> getAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public User getByID(Long id)
    {
        DBValidator.validateId(id);

        try(EntityManager em = emf.createEntityManager())
        {
            User user = em.find(User.class, id);
            return DBValidator.validateExists(user, id, User.class);
        }
    }

    @Override
    public User update(User user)
    {
        DBValidator.validateNotNull(user, "User");
        DBValidator.validateId(user.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                User exist = em.find(User.class, user.getId());
                DBValidator.validateExists(exist, user.getId(), User.class);
                User merged = em.merge(user);
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
                User managed = em.find(User.class, id);
                DBValidator.validateExists(managed, id, User.class);
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

    @Override
    public Optional<User> findByEmail(String email)
    {
        DBValidator.validateNotNull(email, "Email");

        try(EntityManager em = emf.createEntityManager())
        {
            User user = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);

            return Optional.ofNullable(user);
        }
    }

    @Override
    public Set<User> findByRole(UserRole role)
    {
        DBValidator.validateNotNull(role, "UserRole");

        try(EntityManager em = emf.createEntityManager())
        {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.userRole = :role", User.class)
                .setParameter("role", role);
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public boolean existsByEmail(String email)
    {
        DBValidator.validateNotNull(email, "Email");

        try (EntityManager em = emf.createEntityManager())
        {
            Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
            return count > 0;
        }
    }
}
