package app.testutils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

public class TestCleanDB
{
    public static void truncateTables(EntityManagerFactory emf)
    {
        if (emf == null || !emf.isOpen())
        {
            throw new IllegalStateException("EMF is closed in TestCleanDB");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.createNativeQuery(
                """
                TRUNCATE TABLE
                    take_away_order_line,
                    take_away_order,
                    take_away_offer,
                    weekly_menu_slot,
                    weekly_menu,
                    shopping_list_item,
                    shopping_list,
                    ingredient_request,
                    dish,
                    dish_suggestion,
                    allergen,
                    station,
                    users
                RESTART IDENTITY CASCADE
            """).executeUpdate();

            em.getTransaction().commit();

        }
        catch (PersistenceException e)
        {
            throw new RuntimeException("Failed to truncate tables", e);
        }
    }
}
