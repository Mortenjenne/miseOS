package app.services;

import app.persistence.daos.interfaces.IAllergenDAO;
import app.persistence.daos.interfaces.IDishDAO;
import app.persistence.daos.interfaces.IUserReader;

public class DishService
{
    private final IDishDAO dishDAO;
    private final IAllergenDAO allergenDAO;
    private final IUserReader userReader;


    public DishService(IDishDAO dishDAO, IAllergenDAO allergenDAO, IUserReader userReader)
    {
        this.dishDAO = dishDAO;
        this.allergenDAO = allergenDAO;
        this.userReader = userReader;
    }
}
