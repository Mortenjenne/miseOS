package app.config;

import app.persistence.daos.impl.AllergenDAO;
import app.persistence.daos.impl.DishDAO;
import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.daos.interfaces.IDishDAO;
import app.persistence.daos.interfaces.IDishSuggestionDAO;
import jakarta.persistence.EntityManagerFactory;

public class DIContainer
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    private static IAllergenDAO allergenDAO;
    private static IDishDAO dishDAO;
    private static IDishSuggestionDAO dishSuggestionDAO;



    private static synchronized IAllergenDAO getAllergenDAO()
    {
        if(allergenDAO == null)
        {
            allergenDAO = new AllergenDAO(emf);
        }
        return allergenDAO;
    }

    private static synchronized IDishDAO getDishDAO()
    {
        if(dishDAO == null)
        {
            dishDAO = new DishDAO(emf);
        }
        return dishDAO;
    }
}
