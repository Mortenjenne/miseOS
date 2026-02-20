package app.services;

import app.dtos.shopping.CreateShoppingListDTO;
import app.dtos.shopping.ShoppingListDTO;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.IIngredientRequestDAO;
import app.persistence.daos.IShoppingListDAO;
import app.persistence.daos.IUserReader;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.User;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.Set;

public class ShoppingListService
{
    private final IShoppingListDAO shoppingListDAO;
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IUserReader userReader;
    private final IAiClient aiClient;

    public ShoppingListService(IShoppingListDAO shoppingListDAO, IIngredientRequestDAO ingredientRequestDAO, IUserReader userReader, IAiClient aiClient)
    {
        this.shoppingListDAO = shoppingListDAO;
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.userReader = userReader;
        this.aiClient = aiClient;
    }

    public ShoppingListDTO generateShoppingList(CreateShoppingListDTO dto)
    {
        ValidationUtil.validateId(dto.userId());

        User creator = userReader.getByID(dto.userId());
        requireChef(creator);

        Set<IngredientRequest> ingredientRequests = ingredientRequestDAO.findByStatusAndDeliveryDate(Status.APPROVED, dto.deliveryDate());

        if (ingredientRequests.isEmpty())
        {
            throw new IllegalStateException("No approved requests for date: " + ingredientRequests);
        }

        return null;
    }

    private void requireChef(User user)
    {
        if (!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef or sous chef can manage shopping lists");
        }
    }
}
