package app.services.impl;

import app.dtos.security.AuthenticatedUser;
import app.dtos.shopping.*;
import app.enums.ShoppingListStatus;
import app.enums.Status;
import app.exceptions.ConflictException;
import app.mappers.ShoppingListMapper;
import app.persistence.daos.interfaces.IIngredientRequestDAO;
import app.persistence.daos.interfaces.IShoppingListDAO;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.ShoppingList;
import app.persistence.entities.ShoppingListItem;
import app.persistence.entities.User;
import app.services.IAiService;
import app.services.IShoppingListService;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ShoppingListService implements IShoppingListService
{
    private final IShoppingListDAO shoppingListDAO;
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IUserReader userReader;
    private final IAiService aiService;

    private static final String DEFAULT_SUPPLIER = "AB Catering";

    public ShoppingListService(IShoppingListDAO shoppingListDAO, IIngredientRequestDAO ingredientRequestDAO, IUserReader userReader, IAiService aiService)
    {
        this.shoppingListDAO = shoppingListDAO;
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.userReader = userReader;
        this.aiService = aiService;
    }

    @Override
    public ShoppingListDTO generateShoppingList(AuthenticatedUser authUser, CreateShoppingListDTO dto)
    {
        validateAuthenticatedUser(authUser);
        validateCreateInput(dto);
        checkIfShoppingListExists(dto);

        List<IngredientRequest> approvedRequests = ingredientRequestDAO.findByFilter(Status.APPROVED, dto.deliveryDate(),null,null, null);
        checkRequestNotEmpty(approvedRequests, dto.deliveryDate());

        List<String> uniqueIngredientNames = getUniqueIngredientNames(approvedRequests);
        Map<String, String> normalizedNames = aiService.normalizeIngredientList(uniqueIngredientNames, dto.targetLanguage());
        Map<AggregationKey, List<IngredientRequest>> grouped = getIngredientsGrouped(approvedRequests, normalizedNames);

        User creator = userReader.getByID(authUser.userId());
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
        ShoppingList fetchedWithItems = shoppingListDAO.getByID(saved.getId());
        return ShoppingListMapper.toDTO(fetchedWithItems);
    }

    @Override
    public ShoppingListDTO finalizeShoppingList(Long shoppingListId)
    {
        ValidationUtil.validateId(shoppingListId);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        list.finalizeShoppingList();

        ShoppingList finalized = shoppingListDAO.update(list);
        return ShoppingListMapper.toDTO(finalized);
    }

    @Override
    public boolean deleteShoppingList(AuthenticatedUser authUser, Long shoppingListId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(shoppingListId);

        User editor = userReader.getByID(authUser.userId());
        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        list.delete(editor);

        return shoppingListDAO.delete(list.getId());
    }

    @Override
    public List<ShoppingListDTO> getShoppingLists(ShoppingListStatus status, LocalDate deliveryDate)
    {
        return shoppingListDAO.findByFilter(status, deliveryDate)
            .stream()
            .map(ShoppingListMapper::toDTO)
            .toList();
    }

    @Override
    public ShoppingListDTO markItemOrdered(Long shoppingListId, Long itemId)
    {
        validateItemRelatedIds(shoppingListId, itemId);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        ShoppingListItem item = findItemOrThrow(list, itemId);
        item.markAsOrdered();

        ShoppingList updated = shoppingListDAO.update(list);
        return ShoppingListMapper.toDTO(updated);
    }

    @Override
    public ShoppingListDTO markAllItemsOrdered(Long shoppingListId)
    {
        ValidationUtil.validateId(shoppingListId);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        list.getShoppingListItems().forEach(ShoppingListItem::markAsOrdered);

        ShoppingList updated = shoppingListDAO.update(list);
        return ShoppingListMapper.toDTO(updated);
    }

    @Override
    public ShoppingListDTO addItemToShoppingList(AuthenticatedUser authUser, Long shoppingListId, CreateShoppingListItemDTO dto)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(shoppingListId);
        validateItemCreateInput(dto);

        User user = userReader.getByID(authUser.userId());
        String note = "Manual entry by: " + user.getFirstName() + " " + user.getLastName();

        ShoppingListItem item = new ShoppingListItem(
            dto.ingredientName(),
            dto.quantity(),
            dto.unit(),
            dto.supplier(),
            note
        );

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        list.addItem(item);

        ShoppingList updated = shoppingListDAO.update(list);
        return ShoppingListMapper.toDTO(updated);
    }

    @Override
    public ShoppingListDTO removeItem(Long shoppingListId, Long itemId)
    {
        validateItemRelatedIds(shoppingListId, itemId);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        ShoppingListItem item = findItemOrThrow(list, itemId);
        list.removeItem(item);

        ShoppingList updated = shoppingListDAO.update(list);
        return ShoppingListMapper.toDTO(updated);
    }

    @Override
    public ShoppingListDTO updateItem(Long shoppingListId, Long itemId, UpdateShoppingListItemDTO dto)
    {
        validateItemRelatedIds(shoppingListId, itemId);
        validateItemUpdateInput(dto);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        ShoppingListItem item = findItemOrThrow(list, itemId);

        item.update(dto.quantity(), dto.unit(), dto.supplier());

        ShoppingList updated = shoppingListDAO.update(list);
        return ShoppingListMapper.toDTO(updated);
    }

    @Override
    public ShoppingListDTO getById(Long shoppingListId)
    {
        ValidationUtil.validateId(shoppingListId);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        return ShoppingListMapper.toDTO(list);
    }

    @Override
    public ShoppingListDTO updateDeliveryDate(Long shoppingListId, UpdateShoppingListDTO dto)
    {
        ValidationUtil.validateId(shoppingListId);
        ValidationUtil.validateNotNull(dto.deliveryDate(), "Delivery date");
        ValidationUtil.validateNotPastDate(dto.deliveryDate(), "Delivery date");

        shoppingListDAO.findByDeliveryDate(dto.deliveryDate()).ifPresent(existing ->
        {
            if (!existing.getId().equals(shoppingListId))
            {
                throw new ConflictException("A shopping list already exists for: " + dto.deliveryDate());
            }
        });

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        list.updateDeliveryDate(dto.deliveryDate());

        ShoppingList updated = shoppingListDAO.update(list);
        return ShoppingListMapper.toDTO(updated);
    }

    private String getMostCommonSupplier(List<IngredientRequest> requests)
    {
        return requests.stream()
            .map(IngredientRequest::getPreferredSupplier)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(DEFAULT_SUPPLIER);
    }

    private Map<AggregationKey, List<IngredientRequest>> getIngredientsGrouped(List<IngredientRequest> approved, Map<String, String> normalizedNames)
    {
        return approved.stream()
            .collect(Collectors.groupingBy(req -> new AggregationKey(
                normalizedNames.getOrDefault(req.getName(), req.getName()), req.getUnit())
            ));
    }

    private List<String> getUniqueIngredientNames(List<IngredientRequest> ingredientRequests)
    {
        return ingredientRequests.stream()
            .map(IngredientRequest::getName)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private void checkRequestNotEmpty(List<IngredientRequest> requests, LocalDate deliveryDate)
    {
        if (requests == null || requests.isEmpty())
        {
            throw new ConflictException("No approved requests for date: " + deliveryDate);
        }
    }

    private void checkIfShoppingListExists(CreateShoppingListDTO dto)
    {
        Optional<ShoppingList> existing = shoppingListDAO.findByDeliveryDate(dto.deliveryDate());

        if (existing.isPresent())
        {
            throw new ConflictException("Shopping list already exists for date: " + dto.deliveryDate());
        }
    }

    private ShoppingListItem findItemOrThrow(ShoppingList list, Long itemId)
    {
        return list.getShoppingListItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemId));
    }

    private void validateAuthenticatedUser(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
    }

    private void validateCreateInput(CreateShoppingListDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Shopping list");
        ValidationUtil.validateNotNull(dto.deliveryDate(), "Delivery date");
        ValidationUtil.validateNotNull(dto.targetLanguage(), "Target language");
    }

    private void validateItemUpdateInput(UpdateShoppingListItemDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Item update");
        ValidationUtil.validatePositive(dto.quantity(), "Quantity");
        ValidationUtil.validateNotNull(dto.unit(), "Unit");
    }

    private void validateItemCreateInput(CreateShoppingListItemDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Item");
        ValidationUtil.validateName(dto.ingredientName(), "Ingredient name");
        ValidationUtil.validatePositive(dto.quantity(), "Quantity");
        ValidationUtil.validateNotNull(dto.unit(), "Unit");
    }

    private void validateItemRelatedIds(Long shoppingListId, Long itemId)
    {
        ValidationUtil.validateId(shoppingListId);
        ValidationUtil.validateId(itemId);
    }
}
