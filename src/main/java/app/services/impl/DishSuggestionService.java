package app.services.impl;

import app.dtos.dishsuggestion.DishSuggestionCreateDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.dishsuggestion.DishSuggestionFilterDTO;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
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
    public DishSuggestionDTO createSuggestion(Long creatorId, DishSuggestionCreateDTO dto)
    {
        ValidationUtil.validateId(creatorId);
        validateCreateInput(dto);

        Station station = stationReader.getByID(dto.stationId());
        User user = userReader.getByID(creatorId);
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

        int numberOfPendingDishes = dishSuggestionDAO.getPendingSuggestionsCount();
        notificationSender.broadcastPendingUpdate(
            NotificationType.NEW_DISH_SUGGESTIONS,
            NotificationCategory.DISH_SUGGESTION,
            numberOfPendingDishes
        );

        return DishSuggestionMapper.toDTO(saved);
    }

    @Override
    public DishSuggestionDTO approveSuggestion(Long dishId, Long approverId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(approverId);

        DishSuggestion suggestion = dishSuggestionDAO.getByID(dishId);
        User approver = userReader.getByID(approverId);

        requireHeadOrSousChef(approver);
        suggestion.approve(approver);

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

        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(approver);
        notificationSender.notifyStaff(
            updated.getCreatedBy().getId(),
            NotificationType.SUGGESTION_APPROVED,
            NotificationCategory.DISH_SUGGESTION,
            updated.getId(),
            updated.getNameDA(),
            reviewedBy
        );

        broadcastPendingDishSuggestions();
        return DishSuggestionMapper.toDTO(updated);
    }

    @Override
    public DishSuggestionDTO rejectSuggestion(Long dishId, Long approverId, String feedback)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(approverId);
        ValidationUtil.validateNotBlank(feedback, "Feedback");
        ValidationUtil.validateText(feedback, "Feedback", 5, 255);

        DishSuggestion dish = dishSuggestionDAO.getByID(dishId);
        User approver = userReader.getByID(approverId);

        dish.reject(approver, feedback);
        DishSuggestion updated = dishSuggestionDAO.update(dish);

        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(approver);
        notificationSender.notifyStaff(
            updated.getCreatedBy().getId(),
            NotificationType.SUGGESTION_REJECTED,
            NotificationCategory.DISH_SUGGESTION,
            updated.getId(),
            updated.getNameDA(),
            reviewedBy
        );

        broadcastPendingDishSuggestions();
        return DishSuggestionMapper.toDTO(updated);
    }

    @Override
    public DishSuggestionDTO updateSuggestion(Long editorId, Long suggestionId, DishSuggestionUpdateDTO dto)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(suggestionId);
        validateUpdateInput(dto);

        User editor = userReader.getByID(editorId);
        ensureIsKitchenStaff(editor);

        DishSuggestion suggestion = dishSuggestionDAO.getByID(suggestionId);
        Set<Allergen> allergens = fetchAllergens(dto.allergenIds());

        suggestion.updateContent(
            dto.nameDA(),
            dto.descriptionDA(),
            allergens);

        DishSuggestion updated = dishSuggestionDAO.update(suggestion);

        return DishSuggestionMapper.toDTO(updated);
    }

    @Override
    public boolean deleteSuggestion(Long dishId, Long userId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(userId);

        User user = userReader.getByID(userId);
        DishSuggestion suggestion = dishSuggestionDAO.getByID(dishId);

        boolean isCreator = suggestion.getCreatedBy().getId().equals(userId);

        if(!user.isHeadChef() && !user.isSousChef() && !isCreator)
        {
            throw new UnauthorizedActionException("Only head chef, sous chef or creator can delete");
        }

        boolean isDeleted =  dishSuggestionDAO.delete(dishId);

        if(isDeleted)
        {
            broadcastPendingDishSuggestions();
        }
        return isDeleted;
    }

    @Override
    public DishSuggestionDTO getById(Long id)
    {
        ValidationUtil.validateId(id);
        DishSuggestion dish = dishSuggestionDAO.getByID(id);
        return DishSuggestionMapper.toDTO(dish);
    }

    @Override
    public List<DishSuggestionDTO> getByFilter(DishSuggestionFilterDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Filter");

        if (dto.week() != null || dto.year() != null)
        {
            if (dto.week() == null || dto.year() == null)
            {
                throw new IllegalArgumentException("Week and year must both be provided together");
            }
            validateWeekAndYear(dto.week(), dto.year());
        }

        return dishSuggestionDAO.findByFilter(dto.status(), dto.week(), dto.year(), dto.stationId(), dto.orderBy())
            .stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<DishSuggestionDTO> getCurrentWeek(Status status)
    {
        int week = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = LocalDate.now().get(IsoFields.WEEK_BASED_YEAR);


        return dishSuggestionDAO.findByFilter(status, week, year, null, null)
            .stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toList());
    }

    private void broadcastPendingDishSuggestions()
    {
        int remainingPendingDishSuggestions = dishSuggestionDAO.getPendingSuggestionsCount();

        notificationSender.broadcastPendingUpdate(
            NotificationType.PENDING_COUNT_UPDATED,
            NotificationCategory.DISH_SUGGESTION,
            remainingPendingDishSuggestions
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

    private void requireHeadOrSousChef(User user)
    {
        if (!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef or sous chef can approve or reject suggestions");
        }
    }

    private void validateWeekAndYear(int week, int year)
    {
        ValidationUtil.validateRange(week, 1, 53, "Week");
        ValidationUtil.validateRange(year, 2020, 2100, "Year");
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
