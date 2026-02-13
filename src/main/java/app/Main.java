package app;

import app.config.HibernateConfig;

import app.persistence.daos.IUserDAO;
import app.persistence.daos.UserDAO;
import jakarta.persistence.EntityManagerFactory;

public class Main
{
    public static void main(String[] args)
    {
        EntityManagerFactory entityManagerFactory = HibernateConfig.getEntityManagerFactory();
        IUserDAO userDAO = new UserDAO(entityManagerFactory);
    }
}
