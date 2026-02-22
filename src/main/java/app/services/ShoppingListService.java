package app.services;

import app.dtos.shopping.*;
import app.enums.ShoppingListStatus;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.integrations.ai.IAiClient;
import app.persistence.daos.IIngredientRequestDAO;
import app.persistence.daos.IShoppingListDAO;
import app.persistence.daos.IUserReader;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.ShoppingList;
import app.persistence.entities.ShoppingListItem;
import app.persistence.entities.User;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ShoppingListService
{
    private final IShoppingListDAO shoppingListDAO;
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IUserReader userReader;
    private final IAiService aiService;

    public ShoppingListService(IShoppingListDAO shoppingListDAO, IIngredientRequestDAO ingredientRequestDAO, IUserReader userReader, IAiService aiService)
    {
        this.shoppingListDAO = shoppingListDAO;
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.userReader = userReader;
        this.aiService = aiService;
    }

    public ShoppingListDTO generateShoppingList(CreateShoppingListDTO dto)
    {
        ValidationUtil.validateId(dto.userId());

        checkIfShoppingListExists(dto);

        User creator = userReader.getByID(dto.userId());
        requireChef(creator);

        Set<IngredientRequest> approvedRequests = ingredientRequestDAO.findByStatusAndDeliveryDate(Status.APPROVED, dto.deliveryDate());
        checkRequestNotEmpty(approvedRequests, dto.deliveryDate());

        List<String> uniqueIngredientNames = getUniqueIngredientNames(approvedRequests);
        Map<String, String> normalizedNames = aiService.normalizeIngredientList(uniqueIngredientNames, dto.targetLanguage());

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

    public ShoppingListDTO finalizeShoppingList(Long shoppingListId, Long userId)
    {
        ValidationUtil.validateId(shoppingListId);
        ValidationUtil.validateId(userId);

        User approver = userReader.getByID(userId);
        ShoppingList list = shoppingListDAO.getByID(shoppingListId);

        requireChef(approver);

        list.finalizeShoppingList();

        ShoppingList finalized = shoppingListDAO.update(list);

        return mapToShoppingListDTO(finalized);
    }

    public boolean deleteShoppingList(Long shoppingListId, Long userId)
    {
        ValidationUtil.validateId(shoppingListId);
        ValidationUtil.validateId(userId);

        User approver = userReader.getByID(userId);
        ShoppingList list = shoppingListDAO.getByID(shoppingListId);

        requireChef(approver);

        return shoppingListDAO.delete(list.getId());
    }

    public ShoppingListDTO updateShoppingList()
    {
        return null;
    }

    public Set<ShoppingListDTO> getAll()
    {
        return shoppingListDAO.getAll().stream()
            .map(this::mapToShoppingListDTO)
            .collect(Collectors.toSet());
    }

    public Set<ShoppingListDTO> findByStatus(ShoppingListStatus status)
    {
        ValidationUtil.validateNotNull(status, "Status");

        return shoppingListDAO.findByStatus(status).stream()
            .map(this::mapToShoppingListDTO)
            .collect(Collectors.toSet());
    }

    public ShoppingListDTO markItemOrdered(Long shoppingListId, Long itemId, Long userId)
    {
        ValidationUtil.validateId(shoppingListId);
        ValidationUtil.validateId(itemId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        requireChef(user);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);

        ShoppingListItem item = list.getShoppingListItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemId));

        item.markAsOrdered();

        ShoppingList updated = shoppingListDAO.update(list);
        return mapToShoppingListDTO(updated);
    }

    public ShoppingListDTO markAllItemsOrdered(Long shoppingListId, Long userId)
    {
        ValidationUtil.validateId(shoppingListId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        requireChef(user);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);

        list.getShoppingListItems().forEach(ShoppingListItem::markAsOrdered);

        ShoppingList updated = shoppingListDAO.update(list);
        return mapToShoppingListDTO(updated);
    }

    public ShoppingListDTO addItemToShoppingList(CreateShoppingListItemDTO dto)
    {
        ValidationUtil.validateId(dto.shoppingListId());
        ValidationUtil.validateId(dto.userId());

        ShoppingList list = shoppingListDAO.getByID(dto.shoppingListId());
        User user = userReader.getByID(dto.userId());
        requireChef(user);

        if (list.getShoppingListStatus() != ShoppingListStatus.DRAFT)
        {
            throw new IllegalStateException("Cannot add items to finalized list");
        }

        String note = "Manual entry by: " + user.getFirstName() + " " + user.getLastName();

        ShoppingListItem item = new ShoppingListItem(
            dto.ingredientName(),
            dto.quantity(),
            dto.unit(),
            dto.supplier(),
            note
        );

        list.addItem(item);

        ShoppingList updated = shoppingListDAO.update(list);
        return mapToShoppingListDTO(updated);
    }

    public ShoppingListDTO removeItem(Long shoppingListId, Long itemId, Long userId)
    {
        ValidationUtil.validateId(shoppingListId);
        ValidationUtil.validateId(itemId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        requireChef(user);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);

        if (list.getShoppingListStatus() != ShoppingListStatus.DRAFT)
        {
            throw new IllegalStateException("Cannot remove items from finalized list");
        }

        ShoppingListItem item = list.getShoppingListItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemId));

        list.removeItem(item);

        ShoppingList updated = shoppingListDAO.update(list);
        return mapToShoppingListDTO(updated);
    }

    public ShoppingListDTO updateItem(UpdateShoppingListItemDTO dto)
    {
        ValidationUtil.validateId(dto.shoppingListId());
        ValidationUtil.validateId(dto.itemId());
        ValidationUtil.validateId(dto.userId());

        User user = userReader.getByID(dto.userId());
        requireChef(user);

        ShoppingList list = shoppingListDAO.getByID(dto.shoppingListId());

        if (list.getShoppingListStatus() != ShoppingListStatus.DRAFT)
        {
            throw new IllegalStateException("Cannot update items in finalized list");
        }

        ShoppingListItem item = list.getShoppingListItems().stream()
            .filter(i -> i.getId().equals(dto.itemId()))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Item not found: " + dto.itemId()));

        item.update(dto.quantity(), dto.unit(), dto.supplier());

        ShoppingList updated = shoppingListDAO.update(list);
        return mapToShoppingListDTO(updated);
    }

    public ShoppingListDTO getById(Long shoppingListId)
    {
        ValidationUtil.validateId(shoppingListId);

        ShoppingList list = shoppingListDAO.getByID(shoppingListId);
        return mapToShoppingListDTO(list);
    }

    public ShoppingListDTO findByDeliveryDate(LocalDate deliveryDate)
    {
        ValidationUtil.validateNotNull(deliveryDate, "Local date");

        Optional<ShoppingList> list = shoppingListDAO.findByDeliveryDate(deliveryDate);

        if(list.isEmpty())
        {
            throw new EntityNotFoundException("ShoppingList not found");
        }

        return mapToShoppingListDTO(list.get());
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

    private void checkRequestNotEmpty(Set<IngredientRequest> requests, LocalDate deliveryDate)
    {
        if (requests == null || requests.isEmpty())
        {
            throw new IllegalStateException("No approved requests for date: " + deliveryDate);
        }
    }

    private void checkIfShoppingListExists(CreateShoppingListDTO dto)
    {
        Optional<ShoppingList> existing = shoppingListDAO.findByDeliveryDate(dto.deliveryDate());

        if (existing.isPresent())
        {
            throw new IllegalStateException("Shopping list already exists for date: " + dto.deliveryDate());
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
