package app.testutils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class TestCleanDB
{
    public static void truncateTables(EntityManagerFactory emf)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE users, station, allergen, ingredient_request, dish_suggestion  RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
    }
}
