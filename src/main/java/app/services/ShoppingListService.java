package app.services;

import app.dtos.shopping.AggregationKey;
import app.dtos.shopping.CreateShoppingListDTO;
import app.dtos.shopping.ShoppingListDTO;
import app.dtos.shopping.ShoppingListItemDTO;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.IIngredientRequestDAO;
import app.persistence.daos.IShoppingListDAO;
import app.persistence.daos.IUserReader;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.ShoppingList;
import app.persistence.entities.ShoppingListItem;
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
        Map<String, String> normalizedNames = aiClient.normalizeIngredientList(uniqueIngredientNames, dto.targetLanguage());

        Map<AggregationKey, List<IngredientRequest>> grouped = getIngredientsGrouped(approvedRequests, normalizedNames);
        ShoppingList shoppingList = new ShoppingList(dto.deliveryDate(), creator);

        grouped.forEach((aggregationKey, requests) -> {

            Double total = requests.stream()
                .map(IngredientRequest::getQuantity)
                .reduce(0.0, Double::sum);

            String supplier = getMostCommonSupplier(requests);

            String notes = requests.stream()
                .map(req -> String.format("%s (%s: %s %s)",
                    req.getCreatedBy().getFirstName(),
                    req.getName(),
                    req.getQuantity(),
                    req.getUnit()))
                .collect(Collectors.joining(" | "));

            ShoppingListItem item = new ShoppingListItem(aggregationKey.normalizedName(), total, aggregationKey.unit(), supplier, notes);
            shoppingList.addItem(item);
        });

        ShoppingList saved = shoppingListDAO.create(shoppingList);
        return mapToShoppingListDTO(saved);
    }

    private String getMostCommonSupplier(List<IngredientRequest> requests) {
        return requests.stream()
            .map(IngredientRequest::getPreferredSupplier)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("AB Catering");
    }


    private Map<AggregationKey, List<IngredientRequest>> getIngredientsGrouped(Set<IngredientRequest> approved, Map<String, String> normalizedNames)
    {
        return approved.stream()
            .collect(Collectors.groupingBy(req -> new AggregationKey(
                normalizedNames.getOrDefault(req.getName(), req.getName()), req.getUnit())
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

    private ShoppingListDTO mapToShoppingListDTO(ShoppingList shoppingList)
    {
        List<ShoppingListItemDTO> shoppingListItemDTOS = shoppingList.getShoppingListItems().stream()
            .map(this::mapToShoppingItemDTO)
            .toList();

        return new ShoppingListDTO(
            shoppingList.getId(),
            shoppingList.getDeliveryDate(),
            shoppingList.getShoppingListStatus().name(),
            shoppingList.getCreatedBy().getFirstName(),
            shoppingListItemDTOS
        );
    }

    private ShoppingListItemDTO mapToShoppingItemDTO(ShoppingListItem shoppingListItem)
    {
        return new ShoppingListItemDTO(
            shoppingListItem.getId(),
            shoppingListItem.getIngredientName(),
            shoppingListItem.getQuantity(),
            shoppingListItem.getUnit(),
            shoppingListItem.getSupplier(),
            shoppingListItem.getNotes(),
            shoppingListItem.isOrdered()
        );
    }
}
