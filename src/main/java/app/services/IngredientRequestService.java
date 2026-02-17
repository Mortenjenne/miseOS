package app.services;

import app.dtos.CreateIngredientRequestDTO;
import app.dtos.IngredientRequestDTO;
import app.enums.RequestType;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.persistence.daos.IDishSuggestionDAO;
import app.persistence.daos.IIngredientRequestDAO;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.User;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class IngredientRequestService implements IIngredientRequestService
{
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IDishSuggestionDAO dishDAO;


    public IngredientRequestService(IIngredientRequestDAO ingredientRequestDAO, IDishSuggestionDAO dishDAO)
    {
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.dishDAO = dishDAO;
    }

    @Override
    public IngredientRequestDTO createRequest(User creator, CreateIngredientRequestDTO requestDTO)
    {
        ValidationUtil.validateNotNull(creator, "User");
        ValidationUtil.validateNotNull(requestDTO, "Ingredient request");
        ValidationUtil.validateNotBlank(requestDTO.name(), "Ingredient name");
        ValidationUtil.validatePositive(requestDTO.quantity(), "Quantity");
        ValidationUtil.validateNotBlank(requestDTO.unit(), "Unit");
        ValidationUtil.validateFutureDate(requestDTO.deliveryDate(), "Delivery date");

        validateCanCreateRequest(creator);
        validateDeliveryDateRange(requestDTO.deliveryDate());

        DishSuggestion dish = validateAndGetDishSuggestionForRequest(requestDTO.requestType(), requestDTO.dishSuggestionId());

        IngredientRequest ingredientRequest = new IngredientRequest(
            requestDTO.name(),
            requestDTO.quantity(),
            requestDTO.unit(),
            requestDTO.preferredSupplier(),
            requestDTO.note(),
            requestDTO.requestType(),
            requestDTO.deliveryDate(),
            dish,
            creator
        );

        IngredientRequest saved = ingredientRequestDAO.create(ingredientRequest);

        return mapToDTO(saved);
    }

    @Override
    public IngredientRequestDTO approveIngredientRequest(User headChef, Long ingredientRequestId)
    {
        ValidationUtil.validateNotNull(headChef, "User");
        ValidationUtil.validateId(ingredientRequestId);

        validateIsHeadChef(headChef);

        IngredientRequest ingredientRequest = ingredientRequestDAO.getByID(ingredientRequestId);

        validateRequestIsPending(ingredientRequest);
        ingredientRequest.approve(headChef);

        IngredientRequest updated = ingredientRequestDAO.update(ingredientRequest);
        return mapToDTO(updated);
    }

    @Override
    public IngredientRequestDTO rejectIngredientRequest(User headChef, Long requestId)
    {
        ValidationUtil.validateNotNull(headChef, "User");
        ValidationUtil.validateId(requestId);

        validateIsHeadChef(headChef);

        IngredientRequest ingredientRequest = ingredientRequestDAO.getByID(requestId);

        validateRequestIsPending(ingredientRequest);
        ingredientRequest.reject(headChef);

        IngredientRequest updated = ingredientRequestDAO.update(ingredientRequest);
        return mapToDTO(updated);
    }

    @Override
    public Set<IngredientRequestDTO> getAllPendingRequests()
    {
        return ingredientRequestDAO.getAll()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<IngredientRequestDTO> getRequestByStatus(Status status)
    {
        ValidationUtil.validateNotNull(status, "Status");

        return ingredientRequestDAO.findByStatus(status)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<IngredientRequestDTO> getRequestByStatusAndDate(Status status, LocalDate deliveryDate)
    {
        ValidationUtil.validateNotNull(status, "Status");
        ValidationUtil.validateNotNull(deliveryDate, "Delivery date");

        return ingredientRequestDAO.findByStatusAndDeliveryDate(status, deliveryDate)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    @Override
    public IngredientRequestDTO getById(Long id)
    {
        ValidationUtil.validateId(id);

        IngredientRequest request = ingredientRequestDAO.getByID(id);
        return mapToDTO(request);
    }

    @Override
    public IngredientRequestDTO updateRequest(User creator, IngredientRequestDTO requestDTO)
    {
        ValidationUtil.validateNotNull(creator, "User");
        ValidationUtil.validateNotNull(requestDTO, "Ingredient request");
        ValidationUtil.validateNotBlank(requestDTO.name(), "Ingredient name");
        ValidationUtil.validatePositive(requestDTO.quantity(), "Quantity");
        ValidationUtil.validateNotBlank(requestDTO.unit(), "Unit");
        ValidationUtil.validateFutureDate(requestDTO.deliveryDate(), "Delivery date");

        validateCanCreateRequest(creator);
        validateDeliveryDateRange(requestDTO.deliveryDate());

        DishSuggestion dish = validateAndGetDishSuggestionForRequest(requestDTO.requestType(), requestDTO.id());

        IngredientRequest existing = ingredientRequestDAO.getByID(requestDTO.id());
        ValidationUtil.validateNotNull(existing, "Ingredient request");

        existing.setName(requestDTO.name());
        existing.setQuantity(requestDTO.quantity());
        existing.setUnit(requestDTO.unit());
        existing.setNote(requestDTO.note());
        existing.setDeliveryDate(requestDTO.deliveryDate());
        existing.setPreferredSupplier(requestDTO.preferredSupplier());
        existing.setDishSuggestion(dish);

        IngredientRequest updated = ingredientRequestDAO.update(existing);

        return mapToDTO(updated);
    }

    @Override
    public boolean deleteRequest(Long id, User headChef)
    {
        ValidationUtil.validateNotNull(headChef, "User");
        ValidationUtil.validateId(id);

        validateIsHeadChef(headChef);

        return ingredientRequestDAO.delete(id);
    }

    private DishSuggestion validateAndGetDishSuggestionForRequest(RequestType requestType, Long dishId)
    {
        DishSuggestion dish = null;
        if(requestType == RequestType.DISH_SPECIFIC)
        {
            dish = validateDishForIngredientRequest(dishId);
        }
        return dish;
    }

    private void validateCanCreateRequest(User user)
    {
        if (!user.isLineCook() && !user.isHeadChef()) {
            throw new UnauthorizedActionException("Only kitchen staff can create ingredient requests");
        }
    }

    private void validateIsHeadChef(User user)
    {
        if (!user.isHeadChef()) {
            throw new UnauthorizedActionException("Only head chefs can approve/reject requests");
        }
    }

    private void validateDeliveryDateRange(LocalDate date)
    {
        LocalDate maxDate = LocalDate.now().plusDays(30);
        if (date.isAfter(maxDate))
        {
            throw new IllegalArgumentException("Cannot order more than 30 days in advance");
        }
    }

    private DishSuggestion validateDishForIngredientRequest(Long dishId) {
        if (dishId == null)
        {
            throw new ValidationException("Dish ID is required for dish-specific requests");
        }

        DishSuggestion dish = dishDAO.getByID(dishId);

        if (dish.getDishStatus() != Status.APPROVED)
        {
            throw new ValidationException("Can only request ingredients for approved dishes. Current status: " + dish.getDishStatus());
        }

        return dish;
    }

    private void validateRequestIsPending(IngredientRequest request)
    {
        if (request.getRequestStatus() != Status.PENDING)
        {
            throw new IllegalStateException("Can only approve pending requests. Current status: " + request.getRequestStatus());
        }
    }

    private IngredientRequestDTO mapToDTO(IngredientRequest request) {
        return new IngredientRequestDTO(
            request.getId(),
            request.getName(),
            request.getQuantity(),
            request.getUnit(),
            request.getPreferredSupplier(),
            request.getNote(),
            request.getRequestStatus(),
            request.getRequestType(),
            request.getDeliveryDate(),
            request.getCreatedAt(),
            request.getReviewedAt(),
            request.getCreatedBy().getId(),
            request.getDishSuggestion() != null ? request.getDishSuggestion().getId() : null
        );
    }
}
