package app.persistence.daos.impl;

import app.persistence.daos.IUserDAO;
import app.persistence.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements IUserDAO
{
    private final EntityManagerFactory emf;

    public UserDAOImpl(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public User create(User user)
    {
        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public Optional<User> findById(int id)
    {
        try(EntityManager em = emf.createEntityManager())
        {
            User user = em.find(User.class, id);
            return Optional.ofNullable(user);
        }
    }

    @Override
    public Optional<User> findByEmail(String email)
    {
        try(EntityManager em = emf.createEntityManager())
        {
            return em.createQuery(
                "select u from User u where u.email = :email", User.class)
                .setParameter("email",email)
                .getResultStream()
                .findFirst();
        }
    }

    @Override
    public List<User> findAll()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            return em.createQuery(
                "select u from User u", User.class)
                .getResultList();
        }
    }

    @Override
    public User update(User user)
    {
        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            User userMerged = em.merge(user);
            em.getTransaction().commit();
            return userMerged;
        }
    }

    @Override
    public boolean delete(int id)
    {
        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            User user = em.find(User.class, id);

            boolean found = false;
            if(user != null)
            {
                em.remove(user);
                found = true;
            }
            em.getTransaction().commit();
            return found;
        }
    }
}
