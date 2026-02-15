package app.testutils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class TestCleanDB
{
    public static void truncateTables(EntityManagerFactory emf)
    {
        if (emf == null || !emf.isOpen())
            throw new IllegalStateException("EMF is closed in TestCleanDB");

        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.createNativeQuery("""
            TRUNCATE TABLE
                users,
                station,
                allergen,
                ingredient_request,
                dish_suggestion,
                shopping_list,
                shopping_list_item,
                weekly_menu,
                weekly_menu_slot
            RESTART IDENTITY CASCADE
        """).executeUpdate();
            em.getTransaction().commit();
        }
    }

}
