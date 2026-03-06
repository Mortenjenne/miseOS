package app.services.impl;

import app.dtos.dishsuggestion.DishSuggestionCreateDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.mappers.DishSuggestionMapper;
import app.persistence.daos.interfaces.*;
import app.persistence.entities.*;
import app.services.IDishSuggestionService;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class DishSuggestionService implements IDishSuggestionService
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

    @Override
    public DishSuggestionDTO submitSuggestion(Long creatorId, DishSuggestionCreateDTO dto)
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
        return DishSuggestionMapper.toDTO(saved);
    }

    @Override
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
            suggestion.getTargetYear()
        );

        dishDAO.create(dish);

        return DishSuggestionMapper.toDTO(updated);
    }

    @Override
    public DishSuggestionDTO rejectDish(Long dishId, Long approverId, String feedback)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(approverId);
        ValidationUtil.validateNotBlank(feedback, "Feedback");
        ValidationUtil.validateText(feedback, "Feedback", 5, 255);

        DishSuggestion dish = dishSuggestionDAO.getByID(dishId);
        User approver = userReader.getByID(approverId);

        dish.reject(approver, feedback);
        DishSuggestion updated = dishSuggestionDAO.update(dish);

        return DishSuggestionMapper.toDTO(updated);
    }

    @Override
    public DishSuggestionDTO updateDish(Long editorId, Long suggestionId, DishSuggestionUpdateDTO dto)
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

    @Override
    public DishSuggestionDTO getById(Long id)
    {
        ValidationUtil.validateId(id);
        DishSuggestion dish = dishSuggestionDAO.getByID(id);
        return DishSuggestionMapper.toDTO(dish);
    }

    @Override
    public DishSuggestionDTO getByIdWithAllergens(Long id) {
        ValidationUtil.validateId(id);

        return dishSuggestionDAO.getByIdWithAllergens(id)
            .map(DishSuggestionMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("DishSuggestion with ID " + id + " not found"));
    }

    @Override
    public Set<DishSuggestionDTO> getAllDishSuggestions()
    {
        return dishSuggestionDAO.getAll()
            .stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<DishSuggestionDTO> getPendingSuggestions()
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByStatus(Status.PENDING);

        return dishes.stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<DishSuggestionDTO> getPendingForWeek(int week, int year)
    {
        validateWeekAndYear(week,year);

        Set<DishSuggestion> dishes = dishSuggestionDAO.findByWeekYearAndStatus(week, year, Status.PENDING);

        return dishes.stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<DishSuggestionDTO> getApprovedForWeek(int week, int year)
    {
        validateWeekAndYear(week,year);

        Set<DishSuggestion> dishes = dishSuggestionDAO.findByWeekYearAndStatus(week, year, Status.APPROVED);

        return dishes.stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<DishSuggestionDTO> getByStatus(Status status)
    {
        ValidationUtil.validateNotNull(status, "Dish status");

        Set<DishSuggestion> dishes = dishSuggestionDAO.findByStatus(status);

        return dishes.stream()
            .map(DishSuggestionMapper::toDTO)
            .collect(Collectors.toSet());
    }

    private void ensureIsKitchenStaff(User user)
    {
        if(!user.isKitchenStaff())
        {
            throw new UnauthorizedActionException(
                "Only kitchen staff can create dish suggestions"
            );
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
