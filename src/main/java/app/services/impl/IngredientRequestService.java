package app.services.impl;

import app.dtos.ingredient.ApproveIngredientRequestDTO;
import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.dtos.security.AuthenticatedUser;
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
    public IngredientRequestDTO createIngredientRequest(AuthenticatedUser authUser, CreateIngredientRequestDTO dto)
    {
        validateAuthenticatedUser(authUser);
        validateCreateInput(dto);

        User creator = userReader.getByID(authUser.userId());
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

        broadcastNewPendingIngredientRequest();
        return IngredientRequestMapper.toDTO(saved);
    }


    @Override
    public IngredientRequestDTO approveIngredientRequest(AuthenticatedUser authUser, Long ingredientRequestId, ApproveIngredientRequestDTO dto)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(ingredientRequestId);

        User reviewer = userReader.getByID(authUser.userId());
        IngredientRequest request = ingredientRequestDAO.getByID(ingredientRequestId);

        if (dto != null)
        {
            request.adjustQuantityForApproval(dto.quantity(), dto.note());
        }

        request.approve(reviewer);
        IngredientRequest updated = ingredientRequestDAO.update(request);

        notifyStaffIngredientRequestApproved(reviewer, updated);
        broadcastPendingIngredientRequests();
        return IngredientRequestMapper.toDTO(updated);
    }


    @Override
    public IngredientRequestDTO rejectIngredientRequest(AuthenticatedUser authUser, Long ingredientRequestId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(ingredientRequestId);

        User reviewer = userReader.getByID(authUser.userId());
        IngredientRequest request = ingredientRequestDAO.getByID(ingredientRequestId);

        request.reject(reviewer);
        IngredientRequest updated = ingredientRequestDAO.update(request);

        notifyStaffIngredientRequestRejected(reviewer, updated);
        broadcastPendingIngredientRequests();
        return IngredientRequestMapper.toDTO(updated);
    }

    @Override
    public List<IngredientRequestDTO> getRequests(AuthenticatedUser authUser, Status status, LocalDate deliveryDate, RequestType requestType, Long stationId)
    {
        validateAuthenticatedUser(authUser);

        if (stationId != null)
        {
            ValidationUtil.validateId(stationId);
        }

        Long creatorId = authUser.isHeadChef() || authUser.isSousChef() ? null : authUser.userId();

        List<IngredientRequest> ingredientRequests = ingredientRequestDAO.findByFilter(status, deliveryDate, creatorId, requestType, stationId);

        return ingredientRequests.stream()
            .map(IngredientRequestMapper::toDTO)
            .toList();
    }

    @Override
    public IngredientRequestDTO getById(AuthenticatedUser authUser, Long id)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(id);

        IngredientRequest request = ingredientRequestDAO.getByID(id);

        boolean isOwner = request.getCreatedBy() != null && request.getCreatedBy().getId().equals(authUser.userId());

        if (!isOwner && !authUser.isHeadChef() && !authUser.isSousChef())
        {
            throw new UnauthorizedActionException("Only owner, head chef, or sous chef can view this requests");
        }

        return IngredientRequestMapper.toDTO(request);
    }

    @Override
    public IngredientRequestDTO updateRequest(AuthenticatedUser authUser, Long ingredientRequestId, UpdateIngredientRequestDTO dto)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(ingredientRequestId);
        validateUpdateInput(dto);

        User editor = userReader.getByID(authUser.userId());
        validateDeliveryDateRange(dto.deliveryDate());

        IngredientRequest existing = ingredientRequestDAO.getByID(ingredientRequestId);
        Dish dish = validateAndGetDishForRequest(dto.requestType(), dto.dishId(), editor);

        boolean isOwner = existing.getCreatedBy() != null && existing.getCreatedBy().getId().equals(authUser.userId());
        boolean isHeadChefOrSousChef = authUser.isHeadChef() || authUser.isSousChef();

        if (!isOwner && !isHeadChefOrSousChef)
        {
            throw new UnauthorizedActionException("Only owner, head chef, or sous chef can update requests");
        }

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
    public boolean deleteRequest(AuthenticatedUser authUser, Long ingredientRequestId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(ingredientRequestId);

        User requester = userReader.getByID(authUser.userId());
        IngredientRequest ingredientRequest = ingredientRequestDAO.getByID(ingredientRequestId);

        boolean isOwner = ingredientRequest.getCreatedBy() != null && ingredientRequest.getCreatedBy().getId().equals(authUser.userId());
        boolean isHeadChefOrSousChef = authUser.isHeadChef() || authUser.isSousChef();

        if (!isOwner && !isHeadChefOrSousChef)
        {
            throw new UnauthorizedActionException("Only owner, head chef, or sous chef can delete requests");
        }

        ingredientRequest.delete(requester);
        boolean isDeleted = ingredientRequestDAO.delete(ingredientRequestId);

        if(isDeleted)
        {
            broadcastPendingIngredientRequests();
        }
        return isDeleted;
    }

    private void broadcastPendingIngredientRequests()
    {
        int remainingPendingRequests = ingredientRequestDAO.getPendingRequestCount();

        notificationSender.broadcastPendingUpdate(
            NotificationType.PENDING_COUNT_UPDATED,
            NotificationCategory.INGREDIENT_REQUEST,
            remainingPendingRequests
        );
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

    private void notifyStaffIngredientRequestApproved(User requester, IngredientRequest updated)
    {
        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(requester);

        notificationSender.notifyStaff(
            updated.getCreatedBy().getId(),
            NotificationType.REQUEST_APPROVED,
            NotificationCategory.INGREDIENT_REQUEST,
            updated.getId(),
            updated.getName(),
            reviewedBy
        );
    }

    private void notifyStaffIngredientRequestRejected(User requester, IngredientRequest updated)
    {
        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(requester);

        notificationSender.notifyStaff(
            updated.getCreatedBy().getId(),
            NotificationType.REQUEST_REJECTED,
            NotificationCategory.INGREDIENT_REQUEST,
            updated.getId(),
            updated.getName(),
            reviewedBy
        );
    }

    private void broadcastNewPendingIngredientRequest()
    {
        int numberOfPendingRequests = ingredientRequestDAO.getPendingRequestCount();

        notificationSender.broadcastPendingUpdate(
            NotificationType.NEW_INGREDIENT_REQUEST,
            NotificationCategory.INGREDIENT_REQUEST,
            numberOfPendingRequests
        );
    }

    private void validateAuthenticatedUser(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
    }

    private void validateCreateInput(CreateIngredientRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Request");
        ValidationUtil.validateName(dto.name(), "Ingredient name");
        ValidationUtil.validatePositive(dto.quantity(), "Quantity");
        ValidationUtil.validateNotNull(dto.unit(), "Unit");
        ValidationUtil.validateNotNull(dto.deliveryDate(), "Delivery date");
        ValidationUtil.validateNotPastDate(dto.deliveryDate(), "Delivery date");
    }

    private void validateUpdateInput(UpdateIngredientRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Request");
        ValidationUtil.validateName(dto.name(), "Ingredient name");
        ValidationUtil.validatePositive(dto.quantity(), "Quantity");
        ValidationUtil.validateNotNull(dto.unit(), "Unit");
        ValidationUtil.validateNotNull(dto.requestType(), "Request type");
        ValidationUtil.validateNotNull(dto.deliveryDate(), "Delivery date");
        ValidationUtil.validateNotPastDate(dto.deliveryDate(), "Delivery date");
    }
}
