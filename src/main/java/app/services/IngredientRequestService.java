package app.services;

import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.enums.RequestType;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.persistence.daos.interfaces.IDishSuggestionReader;
import app.persistence.daos.interfaces.IIngredientRequestDAO;
import app.persistence.daos.interfaces.IUserReader;
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
    private final IDishSuggestionReader dishReader;
    private final IUserReader userReader;


    public IngredientRequestService(IIngredientRequestDAO ingredientRequestDAO, IDishSuggestionReader dishReader, IUserReader userReader)
    {
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.dishReader = dishReader;
        this.userReader = userReader;
    }

    @Override
    public IngredientRequestDTO createRequest(Long creatorId, CreateIngredientRequestDTO requestDTO)
    {
        ValidationUtil.validateId(creatorId);
        validateCreateInput(requestDTO);

        User creator = userReader.getByID(creatorId);

        requireKitchenStaff(creator);
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
    public IngredientRequestDTO approveIngredientRequest(Long headChefId, Long ingredientRequestId)
    {
        ValidationUtil.validateId(headChefId);
        ValidationUtil.validateId(ingredientRequestId);

        User headChef = userReader.getByID(headChefId);
        IngredientRequest request = ingredientRequestDAO.getByID(ingredientRequestId);

        request.approve(headChef);

        IngredientRequest updated = ingredientRequestDAO.update(request);
        return mapToDTO(updated);
    }

    @Override
    public IngredientRequestDTO rejectIngredientRequest(Long headChefId, Long requestId)
    {
        ValidationUtil.validateId(headChefId);
        ValidationUtil.validateId(requestId);

        User headChef = userReader.getByID(headChefId);
        IngredientRequest request = ingredientRequestDAO.getByID(requestId);

        request.reject(headChef);

        IngredientRequest updated = ingredientRequestDAO.update(request);
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
    public IngredientRequestDTO updateRequest(Long userEditorId, Long requestId, Long dishId, UpdateIngredientRequestDTO dto)
    {
        ValidationUtil.validateId(userEditorId);
        ValidationUtil.validateId(requestId);
        ValidationUtil.validateId(dishId);

        User editor = userReader.getByID(userEditorId);
        requireKitchenStaff(editor);

        validateDeliveryDateRange(dto.deliveryDate());

        DishSuggestion dish = validateAndGetDishSuggestionForRequest(dto.requestType(), dishId);
        IngredientRequest existing = ingredientRequestDAO.getByID(requestId);

        existing.update(
            dto.name(),
            dto.quantity(),
            dto.unit(),
            dto.preferredSupplier(),
            dto.note(),
            dto.deliveryDate(),
            dish
        );

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

    private void requireKitchenStaff(User user)
    {
        if (!user.isLineCook() && !user.isHeadChef())
        {
            throw new UnauthorizedActionException("Only kitchen staff can manage requests");
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

        DishSuggestion dish = dishReader.getByID(dishId);

        if (dish.getDishStatus() != Status.APPROVED)
        {
            throw new ValidationException("Can only request ingredients for approved dishes. Current status: " + dish.getDishStatus());
        }

        return dish;
    }

    private void validateCreateInput(CreateIngredientRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Request");
        ValidationUtil.validateNotBlank(dto.name(), "Ingredient name");
        ValidationUtil.validatePositive(dto.quantity(), "Quantity");
        ValidationUtil.validateNotNull(dto.unit(), "Unit");
        ValidationUtil.validateNotNull(dto.deliveryDate(), "Delivery date");
        ValidationUtil.validateFutureDate(dto.deliveryDate(), "Delivery date");
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
