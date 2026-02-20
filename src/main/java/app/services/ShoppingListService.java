package app.services;

import app.dtos.shopping.CreateShoppingListDTO;
import app.dtos.shopping.ShoppingListDTO;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.IIngredientRequestDAO;
import app.persistence.daos.IShoppingListDAO;
import app.persistence.daos.IUserReader;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.ShoppingList;
import app.persistence.entities.User;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

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

        Set<IngredientRequest> approvedRequests = ingredientRequestDAO.findByStatusAndDeliveryDate(Status.APPROVED, dto.deliveryDate());
        checkRequestNotEmpty(approvedRequests);


        List<String> uniqueIngredientNames = getUniqueIngredientNames(approvedRequests);
        ShoppingList shoppingList = new ShoppingList(dto.deliveryDate(), creator);

        Map<String, String> normalizedIngredientNames = aiClient.normalizeIngredientList(uniqueIngredientNames, dto.targetLanguage());
        Map<String, List<IngredientRequest>> groupedIngredients = getIngredientsGrouped(approvedRequests, normalizedIngredientNames);


        return null;
    }

    private Map<String, List<IngredientRequest>> getIngredientsGrouped(Set<IngredientRequest> approvedRequests, Map<String, String> normalizedIngredientNames)
    {
       return approvedRequests.stream()
                .collect(Collectors.groupingBy(
                    request -> normalizedIngredientNames
                        .getOrDefault(request.getName(), request.getName())
                ));
    }

    private List<String> getUniqueIngredientNames(Set<IngredientRequest> ingredientRequests)
    {
        return ingredientRequests.stream()
            .map(IngredientRequest::getName)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private void requireChef(User user)
    {
        if (!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef or sous chef can manage shopping lists");
        }
    }

    private void checkRequestNotEmpty(Set<IngredientRequest> ingredientRequests)
    {
        if (ingredientRequests == null || ingredientRequests.isEmpty())
        {
            throw new IllegalStateException("No approved requests for date: " + ingredientRequests);
        }
    }
}
