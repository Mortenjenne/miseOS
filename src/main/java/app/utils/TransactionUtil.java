package app.utils;

import jakarta.persistence.EntityManager;

public class TransactionUtil
{
    public static void rollback(EntityManager em)
    {
        if (em != null && em.getTransaction().isActive())
        {
            em.getTransaction().rollback();
        }
    }
}
