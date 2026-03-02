package app.config;

import app.persistence.daos.impl.AllergenDAO;
import app.persistence.daos.impl.DishDAO;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.daos.interfaces.IDishDAO;
import app.persistence.daos.interfaces.IDishSuggestionDAO;
import jakarta.persistence.EntityManagerFactory;

public class DIContainer
{
    private static EntityManagerFactory emf;

    private static IAllergenDAO allergenDAO;
    private static IDishDAO dishDAO;
    private static IDishSuggestionDAO dishSuggestionDAO;

    public static synchronized EntityManagerFactory getEmf()
    {
        if (emf == null)
        {
            emf = HibernateConfig.getEntityManagerFactory();
        }
        return emf;
    }

    public static synchronized IAllergenDAO getAllergenDAO()
    {
        if(allergenDAO == null)
        {
            allergenDAO = new AllergenDAO(getEmf());
        }
        return allergenDAO;
    }

    public static synchronized IDishDAO getDishDAO()
    {
        if(dishDAO == null)
        {
            dishDAO = new DishDAO(getEmf());
        }
        return dishDAO;
    }
}
