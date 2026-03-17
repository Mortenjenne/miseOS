package app.services.impl;

import app.dtos.ingredient.ApproveIngredientRequestDTO;
import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.dtos.user.UserReferenceDTO;
import app.enums.NotificationCategory;
import app.enums.NotificationType;
import app.enums.RequestType;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.mappers.IngredientRequestMapper;
import app.mappers.UserMapper;
import app.persistence.daos.interfaces.readers.IDishReader;
import app.persistence.daos.interfaces.IIngredientRequestDAO;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.Dish;
import app.persistence.entities.IngredientRequest;
import app.persistence.entities.User;
import app.services.IIngredientRequestService;
import app.services.INotificationSender;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class IngredientRequestService implements IIngredientRequestService
{
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IDishReader dishReader;
    private final IUserReader userReader;
    private final INotificationSender notificationSender;


    public IngredientRequestService(IIngredientRequestDAO ingredientRequestDAO, IDishReader dishReader, IUserReader userReader, INotificationSender notificationSender)
    {
        this.ingredientRequestDAO = ingredientRequestDAO;
        this.dishReader = dishReader;
        this.userReader = userReader;
        this.notificationSender = notificationSender;
    }

    @Override
    public IngredientRequestDTO createIngredientRequest(Long creatorId, CreateIngredientRequestDTO dto)
    {
        ValidationUtil.validateId(creatorId);
        validateCreateInput(dto);

        User creator = userReader.getByID(creatorId);
        requireKitchenStaff(creator);
        validateDeliveryDateRange(dto.deliveryDate());

        Dish dish = validateAndGetDishForRequest(dto.requestType(), dto.dishId(), creator);

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

        int numberOfPendingRequests = ingredientRequestDAO.getPendingRequestCount();
        notificationSender.sendPendingUpdate(
            NotificationType.NEW_INGREDIENT_REQUEST,
            NotificationCategory.INGREDIENT_REQUEST,
            numberOfPendingRequests
        );

        return IngredientRequestMapper.toDTO(saved);
    }

    @Override
    public IngredientRequestDTO approveIngredientRequest(Long requesterId, Long ingredientRequestId, ApproveIngredientRequestDTO dto)
    {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(ingredientRequestId);

        User requester = userReader.getByID(requesterId);
        IngredientRequest request = ingredientRequestDAO.getByID(ingredientRequestId);

        if (dto != null)
        {
            request.adjustQuantityForApproval(dto.quantity(), dto.note());
        }

        request.approve(requester);

        IngredientRequest updated = ingredientRequestDAO.update(request);

        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(requester);
        notificationSender.notifyStaff(
            updated.getCreatedBy().getId(),
            NotificationType.REQUEST_APPROVED,
            NotificationCategory.INGREDIENT_REQUEST,
            updated.getId(),
            updated.getName(),
            reviewedBy
        );

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

        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(requester);
        notificationSender.notifyStaff(
            updated.getCreatedBy().getId(),
            NotificationType.REQUEST_REJECTED,
            NotificationCategory.INGREDIENT_REQUEST,
            updated.getId(),
            updated.getName(),
            reviewedBy
        );

        return IngredientRequestMapper.toDTO(updated);
    }

    @Override
    public List<IngredientRequestDTO> getRequests(Long userId, Status status, LocalDate deliveryDate, RequestType requestType, Long stationId)
    {
        ValidationUtil.validateId(userId);
        if (stationId != null)
        {
            ValidationUtil.validateId(stationId);
        }

        User user = userReader.getByID(userId);
        Long creatorId = user.isHeadChef() || user.isSousChef() ? null : userId;

        List<IngredientRequest> ingredientRequests = ingredientRequestDAO.findByFilter(status, deliveryDate, creatorId, requestType, stationId);

        return ingredientRequests.stream()
            .map(IngredientRequestMapper::toDTO)
            .toList();
    }

    @Override
    public IngredientRequestDTO getById(Long requesterId, Long id)
    {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(id);

        User user = userReader.getByID(requesterId);
        IngredientRequest request = ingredientRequestDAO.getByID(id);

        boolean isOwner = request.getCreatedBy() != null && request.getCreatedBy().getId().equals(requesterId);

        if (!isOwner && !user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Not allowed to view this ingredient request");
        }

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

        Dish dish = validateAndGetDishForRequest(dto.requestType(), dto.dishId(), editor);
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

    private Dish validateAndGetDishForRequest(RequestType requestType, Long dishId, User creator)
    {
        Dish dish = null;

        if(requestType == RequestType.DISH_SPECIFIC)
        {
            dish = validateDishForIngredientRequest(dishId, creator);
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

    private Dish validateDishForIngredientRequest(Long dishId, User creator) {
        if (dishId == null)
        {
            throw new ValidationException("Dish ID is required for dish-specific requests");
        }

        Dish dish = dishReader.getByID(dishId);

        if(!dish.isActive())
        {
            throw new ValidationException("Dish must be active");
        }

        boolean isHeadChefOrSousChef = creator.isHeadChef() || creator.isSousChef();

        if (!isHeadChefOrSousChef)
        {
            if (creator.getStation() == null)
            {
                throw new ValidationException("You are not assigned to a station — contact your head chef");
            }

            if (!creator.getStation().getId().equals(dish.getStation().getId()))
            {
                throw new UnauthorizedActionException("Dish does not belong to your station");
            }
        }

        return dish;
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
