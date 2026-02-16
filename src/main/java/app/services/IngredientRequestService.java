package app.services;

import app.dtos.CreateIngredientRequestDTO;
import app.dtos.IngredientRequestDTO;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.IDishSuggestionDAO;
import app.persistence.daos.IIngredientRequestDAO;
import app.persistence.daos.IUserDAO;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.User;

public class IngredientRequestService
{
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IDishSuggestionDAO dishDAO;


    public IngredientRequestService(IIngredientRequestDAO ingredientRequestDAO, IDishSuggestionDAO dishDAO)
    {
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.dishDAO = dishDAO;
    }

    public IngredientRequestDTO createRequest(User create, CreateIngredientRequestDTO requestDTO)
    {

    }




}
