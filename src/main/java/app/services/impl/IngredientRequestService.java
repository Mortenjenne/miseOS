package app.services.impl;

import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.enums.RequestType;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.mappers.IngredientRequestMapper;
import app.persistence.daos.interfaces.readers.IDishReader;
import app.persistence.daos.interfaces.IIngredientRequestDAO;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.Dish;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.User;
import app.services.IIngredientRequestService;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class IngredientRequestService implements IIngredientRequestService
{
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IDishReader dishReader;
    private final IUserReader userReader;


    public IngredientRequestService(IIngredientRequestDAO ingredientRequestDAO, IDishReader dishReader, IUserReader userReader)
    {
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.dishReader = dishReader;
        this.userReader = userReader;
    }

    @Override
    public IngredientRequestDTO createIngredientRequest(Long creatorId, CreateIngredientRequestDTO dto)
    {
        ValidationUtil.validateId(creatorId);
        validateCreateInput(dto);

        User creator = userReader.getByID(creatorId);
        requireKitchenStaff(creator);
        validateDeliveryDateRange(dto.deliveryDate());

        Dish dish = validateAndGetDishForRequest(dto.requestType(), dto.dishId());

        IngredientRequest ingredientRequest = new IngredientRequest(
            dto.name(),
            dto.quantity(),
            dto.unit(),
            dto.preferredSupplier(),
            dto.note(),
            dto.requestType(),
            dto.deliveryDate(),
            dish,
            creator
        );

        IngredientRequest saved = ingredientRequestDAO.create(ingredientRequest);
        return IngredientRequestMapper.toDTO(saved);
    }

    @Override
    public IngredientRequestDTO approveIngredientRequest(Long requesterId, Long ingredientRequestId)
    {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(ingredientRequestId);

        User requester = userReader.getByID(requesterId);
        IngredientRequest request = ingredientRequestDAO.getByID(ingredientRequestId);

        request.approve(requester);

        IngredientRequest updated = ingredientRequestDAO.update(request);
        return IngredientRequestMapper.toDTO(updated);
    }

    @Override
    public IngredientRequestDTO rejectIngredientRequest(Long requesterId, Long ingredientRequestId)
    {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(ingredientRequestId);

        User requester = userReader.getByID(requesterId);
        IngredientRequest request = ingredientRequestDAO.getByID(ingredientRequestId);

        request.reject(requester);

        IngredientRequest updated = ingredientRequestDAO.update(request);
        return IngredientRequestMapper.toDTO(updated);
    }

    @Override
    public List<IngredientRequestDTO> getRequests(Long userId, Status status, LocalDate deliveryDate, RequestType requestType)
    {
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        Long creatorId = user.isHeadChef() || user.isSousChef() ? null : userId;

        List<IngredientRequest> ingredientRequests = ingredientRequestDAO.findByFilter(status, deliveryDate, creatorId, requestType);

        return ingredientRequests.stream()
            .map(IngredientRequestMapper::toDTO)
            .toList();
    }

    @Override
    public IngredientRequestDTO getById(Long id)
    {
        ValidationUtil.validateId(id);

        IngredientRequest request = ingredientRequestDAO.getByID(id);
        return IngredientRequestMapper.toDTO(request);
    }

    @Override
    public IngredientRequestDTO updateRequest(Long editorId, Long ingredientRequestId, UpdateIngredientRequestDTO dto)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(ingredientRequestId);
        validateUpdateInput(dto);

        User editor = userReader.getByID(editorId);
        requireKitchenStaff(editor);

        validateDeliveryDateRange(dto.deliveryDate());

        Dish dish = validateAndGetDishForRequest(dto.requestType(), dto.dishId());
        IngredientRequest existing = ingredientRequestDAO.getByID(ingredientRequestId);

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

        return IngredientRequestMapper.toDTO(updated);
    }

    @Override
    public boolean deleteRequest(Long requesterId, Long ingredientRequestId)
    {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(ingredientRequestId);

        User requester = userReader.getByID(requesterId);
        IngredientRequest ingredientRequest = ingredientRequestDAO.getByID(ingredientRequestId);

        ingredientRequest.delete(requester);
        return ingredientRequestDAO.delete(ingredientRequestId);
    }

    private Dish validateAndGetDishForRequest(RequestType requestType, Long dishId)
    {
        Dish dish = null;

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

    private void validateDeliveryDateRange(LocalDate date)
    {
        LocalDate maxDate = LocalDate.now().plusDays(30);
        if (date.isAfter(maxDate))
        {
            throw new IllegalArgumentException("Cannot order more than 30 days in advance");
        }
    }

    private Dish validateDishForIngredientRequest(Long dishId) {
        if (dishId == null)
        {
            throw new ValidationException("Dish ID is required for dish-specific requests");
        }

        return dishReader.getByID(dishId);
    }

    private void validateCreateInput(CreateIngredientRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Request");
        ValidationUtil.validateName(dto.name(), "Ingredient name");
        ValidationUtil.validatePositive(dto.quantity(), "Quantity");
        ValidationUtil.validateNotNull(dto.unit(), "Unit");
        ValidationUtil.validateNotNull(dto.deliveryDate(), "Delivery date");
        ValidationUtil.validateFutureDate(dto.deliveryDate(), "Delivery date");
    }

    private void validateUpdateInput(UpdateIngredientRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Request");
        ValidationUtil.validateName(dto.name(), "Ingredient name");
        ValidationUtil.validatePositive(dto.quantity(), "Quantity");
        ValidationUtil.validateNotNull(dto.unit(), "Unit");
        ValidationUtil.validateNotNull(dto.requestType(), "Request type");
        ValidationUtil.validateNotNull(dto.deliveryDate(), "Delivery date");
        ValidationUtil.validateFutureDate(dto.deliveryDate(), "Delivery date");
    }
}
