package app.services.impl;

import app.dtos.dishsuggestion.DishSuggestionCreateDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.dishsuggestion.DishSuggestionFilterDTO;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
import app.dtos.security.AuthenticatedUser;
import app.dtos.user.UserReferenceDTO;
import app.enums.NotificationCategory;
import app.enums.NotificationType;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.mappers.DishSuggestionMapper;
import app.mappers.UserMapper;
import app.persistence.daos.interfaces.*;
import app.persistence.daos.interfaces.readers.IStationReader;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.*;
import app.services.IDishSuggestionService;
import app.services.INotificationSender;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DishSuggestionService implements IDishSuggestionService
{
    private final IDishSuggestionDAO dishSuggestionDAO;
    private final IDishDAO dishDAO;
    private final IUserReader userReader;
    private final IStationReader stationReader;
    private final IAllergenDAO allergenDAO;
    private final INotificationSender notificationSender;

    public DishSuggestionService(IDishSuggestionDAO dishSuggestionDAO, IDishDAO dishDAO, IUserReader userReader, IStationReader stationReader, IAllergenDAO allergenDAO, INotificationSender notificationSender)
    {
        this.dishSuggestionDAO = dishSuggestionDAO;
        this.dishDAO = dishDAO;
        this.userReader = userReader;
        this.stationReader = stationReader;
        this.allergenDAO = allergenDAO;
        this.notificationSender = notificationSender;
    }

    @Override
    public DishSuggestionDTO createSuggestion(AuthenticatedUser authUser, DishSuggestionCreateDTO dto)
    {
        validateAuthenticatedUser(authUser);
        validateCreateInput(dto);

        Station station = stationReader.getByID(dto.stationId());
        User user = userReader.getByID(authUser.userId());
        ensureIsKitchenStaff(user);

        Set<Allergen> allergens = fetchAllergens(dto.allergenIds());

        DishSuggestion dishRequest = new DishSuggestion(
            dto.nameDA(),
            dto.descriptionDA(),
            dto.targetWeek(),
            dto.targetYear(),
            station,
            user,
            allergens
        );

        dishRequest.checkCreationAllowed(LocalDate.now());

        DishSuggestion saved = dishSuggestionDAO.create(dishRequest);
        broadcastNewPendingDishSuggestion();

        return DishSuggestionMapper.toDTO(saved);
    }

    @Override
    public DishSuggestionDTO approveSuggestion(AuthenticatedUser authUser, Long dishId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(dishId);

        DishSuggestion suggestion = dishSuggestionDAO.getByID(dishId);
        User reviewer = userReader.getByID(authUser.userId());

        suggestion.approve(reviewer);

        DishSuggestion updated = dishSuggestionDAO.update(suggestion);

        Dish dish = new Dish(
            updated.getNameDA(),
            updated.getDescriptionDA(),
            updated.getStation(),
            updated.getAllergens(),
            updated.getCreatedBy(),
            updated.getTargetWeek(),
            updated.getTargetYear()
        );

        dishDAO.create(dish);

        notifyStaffSuggestionApproved(reviewer, updated);
        broadcastRemainingPendingDishSuggestions();

        return DishSuggestionMapper.toDTO(updated);
    }

    @Override
    public DishSuggestionDTO rejectSuggestion(AuthenticatedUser authUser, Long dishId, String feedback)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateNotBlank(feedback, "Feedback");
        ValidationUtil.validateText(feedback, "Feedback", 5, 255);
        validateAuthenticatedUser(authUser);

        DishSuggestion dish = dishSuggestionDAO.getByID(dishId);
        User reviewer = userReader.getByID(authUser.userId());

        dish.reject(reviewer, feedback);
        DishSuggestion updated = dishSuggestionDAO.update(dish);

        notifyStaffSuggestionRejected(reviewer, updated);
        broadcastRemainingPendingDishSuggestions();

        return DishSuggestionMapper.toDTO(updated);
    }

    @Override
    public DishSuggestionDTO updateSuggestion(AuthenticatedUser authUser, Long dishId, DishSuggestionUpdateDTO dto)
    {
        ValidationUtil.validateId(dishId);
        validateUpdateInput(dto);

        DishSuggestion suggestion = dishSuggestionDAO.getByID(dishId);

        boolean isCreator = suggestion.getCreatedBy().getId().equals(authUser.userId());
        boolean isHeadChefOrSousChef = authUser.isHeadChef() || authUser.isSousChef();

        if (!isCreator && !isHeadChefOrSousChef)
        {
            throw new UnauthorizedActionException("You can only update your own suggestions");
        }

        Set<Allergen> allergens = fetchAllergens(dto.allergenIds());

        suggestion.updateContent(
            dto.nameDA(),
            dto.descriptionDA(),
            allergens);

        DishSuggestion updated = dishSuggestionDAO.update(suggestion);

        return DishSuggestionMapper.toDTO(updated);
    }

    @Override
    public boolean deleteSuggestion(AuthenticatedUser authUser, Long dishId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(dishId);

        DishSuggestion suggestion = dishSuggestionDAO.getByID(dishId);

        boolean isCreator = suggestion.getCreatedBy().getId().equals(authUser.userId());

        if(!authUser.isHeadChef() && !authUser.isSousChef() && !isCreator)
        {
            throw new UnauthorizedActionException("Only head chef, sous chef or creator can delete");
        }

        boolean isDeleted =  dishSuggestionDAO.delete(dishId);

        if(isDeleted)
        {
            broadcastRemainingPendingDishSuggestions();
        }
        return isDeleted;
    }

    @Override
    public DishSuggestionDTO getById(AuthenticatedUser authUser, Long dishId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(dishId);
        DishSuggestion suggestion = dishSuggestionDAO.getByID(dishId);

        boolean isCreator = suggestion.getCreatedBy().getId().equals(authUser.userId());
        boolean isHeadChefOrSousChef = authUser.isHeadChef() || authUser.isSousChef();

        if (!isCreator && !isHeadChefOrSousChef)
        {
            throw new UnauthorizedActionException("You can only view your own suggestions");
        }

        return DishSuggestionMapper.toDTO(suggestion);
    }

    @Override
    public List<DishSuggestionDTO> getByFilter(AuthenticatedUser authUser, DishSuggestionFilterDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Filter");
        validateAuthenticatedUser(authUser);

        if (dto.week() != null || dto.year() != null)
        {
            if (dto.week() == null || dto.year() == null)
            {
                throw new IllegalArgumentException("Week and year must both be provided together");
            }
            validateWeekAndYear(dto.week(), dto.year());
        }

        Long creatorId = authUser.isHeadChef() || authUser.isSousChef() ? null : authUser.userId();

        return dishSuggestionDAO.findByFilter(dto.status(), creatorId, dto.week(), dto.year(), dto.stationId(), dto.orderBy())
            .stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<DishSuggestionDTO> getCurrentWeek(Status status)
    {
        int week = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = LocalDate.now().get(IsoFields.WEEK_BASED_YEAR);

        return dishSuggestionDAO.findByFilter(status, null, week, year, null, null)
            .stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public DishSuggestionDTO removeAllergen(AuthenticatedUser authUser, Long suggestionId, Long allergenId)
    {
        validateAuthenticatedUser(authUser);
        ValidationUtil.validateId(suggestionId);
        ValidationUtil.validateId(allergenId);

        DishSuggestion suggestion = dishSuggestionDAO.getByID(suggestionId);

        boolean isCreator = suggestion.getCreatedBy().getId().equals(authUser.userId());
        boolean isHeadChefOrSousChef = authUser.isHeadChef() || authUser.isSousChef();

        if (!isCreator && !isHeadChefOrSousChef)
        {
            throw new UnauthorizedActionException("You can only remove allergen on your own suggestions");
        }

        Allergen allergen = allergenDAO.getByID(allergenId);
        suggestion.removeAllergen(allergen);

        DishSuggestion updated = dishSuggestionDAO.update(suggestion);
        return DishSuggestionMapper.toDTO(updated);
    }

    private void notifyStaffSuggestionApproved(User approver, DishSuggestion updated)
    {
        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(approver);
        notificationSender.notifyStaff(
            updated.getCreatedBy().getId(),
            NotificationType.SUGGESTION_APPROVED,
            NotificationCategory.DISH_SUGGESTION,
            updated.getId(),
            updated.getNameDA(),
            reviewedBy
        );
    }

    private void notifyStaffSuggestionRejected(User rejecter, DishSuggestion updated)
    {
        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(rejecter);
        notificationSender.notifyStaff(
            updated.getCreatedBy().getId(),
            NotificationType.SUGGESTION_REJECTED,
            NotificationCategory.DISH_SUGGESTION,
            updated.getId(),
            updated.getNameDA(),
            reviewedBy
        );
    }

    private void broadcastRemainingPendingDishSuggestions()
    {
        int remainingPendingDishSuggestions = dishSuggestionDAO.getPendingSuggestionsCount();

        notificationSender.broadcastPendingUpdate(
            NotificationType.PENDING_COUNT_UPDATED,
            NotificationCategory.DISH_SUGGESTION,
            remainingPendingDishSuggestions
        );
    }

    private void broadcastNewPendingDishSuggestion()
    {
        int numberOfPendingDishes = dishSuggestionDAO.getPendingSuggestionsCount();

        notificationSender.broadcastPendingUpdate(
            NotificationType.NEW_DISH_SUGGESTIONS,
            NotificationCategory.DISH_SUGGESTION,
            numberOfPendingDishes
        );
    }

    private void ensureIsKitchenStaff(User user)
    {
        if(!user.isKitchenStaff())
        {
            throw new UnauthorizedActionException("Only kitchen staff can create dish suggestions");
        }
    }

    private Set<Allergen> fetchAllergens(Set<Long> allergenIds)
    {
        if (allergenIds == null || allergenIds.isEmpty()) {
            return Set.of();
        }

        return allergenIds.stream()
            .map(allergenDAO::getByID)
            .collect(Collectors.toSet());
    }

    private void validateWeekAndYear(int week, int year)
    {
        ValidationUtil.validateRange(week, 1, 53, "Week");
        ValidationUtil.validateRange(year, 2020, 2100, "Year");
    }

    private void validateAuthenticatedUser(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());
    }

    private void validateCreateInput(DishSuggestionCreateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Dish Suggestion");
        ValidationUtil.validateId(dto.stationId());
        ValidationUtil.validateName(dto.nameDA(), "Name");
        ValidationUtil.validateDescription(dto.descriptionDA(), "Description");
        ValidationUtil.validateRange(dto.targetWeek(), 1, 53, "Target week");
        ValidationUtil.validateRange(dto.targetYear(), 2020, 2100, "Target year");
    }

    private void validateUpdateInput(DishSuggestionUpdateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Suggestion");
        ValidationUtil.validateName(dto.nameDA(), "Name");
        ValidationUtil.validateDescription(dto.descriptionDA(), "Description");
    }
}
