package app.services;

import app.dtos.CreateIngredientRequestDTO;
import app.dtos.IngredientRequestDTO;
import app.enums.RequestType;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.IDishSuggestionDAO;
import app.persistence.daos.IIngredientRequestDAO;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.User;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class IngredientRequestService
{
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IDishSuggestionDAO dishDAO;


    public IngredientRequestService(IIngredientRequestDAO ingredientRequestDAO, IDishSuggestionDAO dishDAO)
    {
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.dishDAO = dishDAO;
    }

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

        DishSuggestion dish = null;
        if(requestDTO.requestType() == RequestType.DISH_SPECIFIC)
        {
            dish = validateDishForIngredientRequest(requestDTO.dishSuggestionId());
        }

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

    public Set<IngredientRequestDTO> getAllPendingRequests()
    {
        return ingredientRequestDAO.getAll()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    public Set<IngredientRequestDTO> getRequestByStatus(Status status)
    {
        return ingredientRequestDAO.findByStatus(status)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    public IngredientRequestDTO getById(Long id)
    {
        ValidationUtil.validateId(id);

        IngredientRequest request = ingredientRequestDAO.getByID(id);
        return mapToDTO(request);
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
            throw new IllegalArgumentException("Dish ID is required for dish-specific requests");
        }

        DishSuggestion dish = dishDAO.getByID(dishId);

        if (dish.getDishStatus() != Status.APPROVED)
        {
            throw new IllegalArgumentException("Can only request ingredients for approved dishes. Current status: " + dish.getDishStatus());
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
