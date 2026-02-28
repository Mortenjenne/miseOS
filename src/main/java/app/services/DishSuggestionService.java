package app.services;

import app.dtos.dish.DishSuggestionCreateDTO;
import app.dtos.dish.DishSuggestionDTO;
import app.dtos.dish.DishSuggestionUpdateDTO;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.interfaces.*;
import app.persistence.entities.*;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class DishSuggestionService
{
    private final IDishSuggestionDAO dishSuggestionDAO;
    private final IDishDAO dishDAO;
    private final IUserReader userReader;
    private final IStationReader stationReader;
    private final IAllergenDAO allergenDAO;

    public DishSuggestionService(IDishSuggestionDAO dishSuggestionDAO, IDishDAO dishDAO, IUserReader userReader, IStationReader stationReader, IAllergenDAO allergenDAO)
    {
        this.dishSuggestionDAO = dishSuggestionDAO;
        this.dishDAO = dishDAO;
        this.userReader = userReader;
        this.stationReader = stationReader;
        this.allergenDAO = allergenDAO;
    }

    public DishSuggestionDTO submitSuggestion(Long creatorId, DishSuggestionCreateDTO dto)
    {
        ValidationUtil.validateId(dto.stationId());
        ValidationUtil.validateId(creatorId);
        validateCreateInput(dto);

        Station station = stationReader.getByID(dto.stationId());
        User user = userReader.getByID(creatorId);
        user.ensureIsKitchenStaff();

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
        return mapToDTO(saved);
    }

    public DishSuggestionDTO approveDish(Long dishId, Long approverId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(approverId);

        DishSuggestion suggestion = dishSuggestionDAO.getByID(dishId);
        User approver = userReader.getByID(approverId);

        suggestion.approve(approver);
        DishSuggestion updated = dishSuggestionDAO.update(suggestion);

        Dish dish = new Dish(
            suggestion.getNameDA(),
            suggestion.getDescriptionDA(),
            suggestion.getStation(),
            suggestion.getAllergens(),
            suggestion.getCreatedBy(),
            suggestion.getTargetWeek(),
            suggestion.getTargetWeek()
        );

        dishDAO.create(dish);

        return mapToDTO(updated);
    }

    public DishSuggestionDTO rejectDish(Long dishId, Long approverId, String feedback)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(approverId);

        DishSuggestion dish = dishSuggestionDAO.getByID(dishId);
        User approver = userReader.getByID(approverId);

        dish.reject(approver, feedback);
        DishSuggestion updated = dishSuggestionDAO.update(dish);

        return mapToDTO(updated);
    }

    public DishSuggestionDTO updateDish(Long editorId, Long suggestionId, DishSuggestionUpdateDTO dto)
    {
        ValidationUtil.validateId(editorId);
        ValidationUtil.validateId(suggestionId);
        validateUpdateInput(dto);

        User editor = userReader.getByID(editorId);
        editor.ensureIsKitchenStaff();

        DishSuggestion suggestion = dishSuggestionDAO.getByID(suggestionId);
        Set<Allergen> allergens = fetchAllergens(dto.allergenIds());

        suggestion.updateContent(
            dto.nameDA(),
            dto.descriptionDA(),
            allergens);

        DishSuggestion updated = dishSuggestionDAO.update(suggestion);

        return mapToDTO(updated);
    }

    public boolean deleteDish(Long dishId, Long userId)
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

        return dishSuggestionDAO.delete(dishId);
    }

    public DishSuggestionDTO getById(Long id)
    {
        ValidationUtil.validateId(id);
        return mapToDTO(dishSuggestionDAO.getByID(id));
    }

    public DishSuggestionDTO getByIdWithAllergens(Long id) {
        ValidationUtil.validateId(id);

        return dishSuggestionDAO.getByIdWithAllergens(id)
            .map(this::mapToDTO)
            .orElseThrow(() -> new EntityNotFoundException("DishSuggestion with ID " + id + " not found"));
    }

    public Set<DishSuggestionDTO> getAllDishSuggestions()
    {
        return dishSuggestionDAO.getAll()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    public Set<DishSuggestionDTO> getPendingSuggestions()
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByStatus(Status.PENDING);

        return dishes.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    public Set<DishSuggestionDTO> getPendingForWeek(int week, int year)
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByWeekYearAndStatus(week, year, Status.PENDING);

        return dishes.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    public Set<DishSuggestionDTO> getApprovedForWeek(int week, int year)
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByWeekYearAndStatus(week, year, Status.APPROVED);

        return dishes.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    public Set<DishSuggestionDTO> getByStatus(Status status)
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByStatus(status);

        return dishes.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    private void validateDeadlineNotPassed(int targetWeek, int targetYear)
    {
        LocalDate targetMonday = LocalDate.of(targetYear, 1, 1)
            .with(java.time.temporal.WeekFields.ISO.weekOfYear(), targetWeek)
            .with(java.time.temporal.WeekFields.ISO.dayOfWeek(), 1);

        LocalDate deadline = targetMonday.minusDays(4);
        LocalDate today = LocalDate.now();

        if (!today.isBefore(deadline))
        {
            throw new IllegalArgumentException("Cannot create suggestion: deadline for week " + targetWeek + " was " + deadline);
        }
    }

    private Set<Allergen> fetchAllergens(Set<Long> ids)
    {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        return ids.stream()
            .map(allergenDAO::getByID)
            .collect(Collectors.toSet());
    }

    private void validateCreateInput(DishSuggestionCreateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Suggestion");
        ValidationUtil.validateNotBlank(dto.nameDA(), "Name");
        ValidationUtil.validateNotBlank(dto.descriptionDA(), "Description");
        ValidationUtil.validateId(dto.stationId());
        ValidationUtil.validateRange(dto.targetWeek(), 1, 53, "Target week");
        ValidationUtil.validateRange(dto.targetYear(), 2020, 2100, "Target year");
    }

    private void validateUpdateInput(DishSuggestionUpdateDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Suggestion");
        ValidationUtil.validateNotBlank(dto.nameDA(), "Name");
        ValidationUtil.validateNotBlank(dto.descriptionDA(), "Description");
    }

    private DishSuggestionDTO mapToDTO(DishSuggestion dish)
    {
        return new DishSuggestionDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getDescriptionDA(),
            dish.getDishStatus(),
            dish.getFeedback(),
            dish.getStation().getStationName(),
            dish.getCreatedBy().getFirstName() + " " + dish.getCreatedBy().getLastName(),
            dish.getAllergens().stream().map(Allergen::getName).collect(Collectors.toSet()),
            dish.getTargetWeek(),
            dish.getTargetYear()
        );
    }
}
