package app.services;

import app.dtos.dish.DishCreateRequestDTO;
import app.dtos.dish.DishSuggestionDTO;
import app.dtos.dish.DishUpdateRequestDTO;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.IAllergenDAO;
import app.persistence.daos.IDishSuggestionDAO;
import app.persistence.daos.IStationDAO;
import app.persistence.daos.IUserDAO;
import app.persistence.entities.Allergen;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DishSuggestionService
{
    private final IDishSuggestionDAO dishSuggestionDAO;
    private final IUserDAO userDAO;
    private final IStationDAO stationDAO;
    private final IAllergenDAO allergenDAO;

    public DishSuggestionService(IDishSuggestionDAO dishSuggestionDAO, IUserDAO userDAO, IStationDAO stationDAO, IAllergenDAO allergenDAO)
    {
        this.dishSuggestionDAO = dishSuggestionDAO;
        this.userDAO = userDAO;
        this.stationDAO = stationDAO;
        this.allergenDAO = allergenDAO;
    }

    public DishSuggestionDTO submitSuggestion(DishCreateRequestDTO dto)
    {
        ValidationUtil.validateId(dto.stationId());
        ValidationUtil.validateId(dto.userCreatedById());

        Station station = stationDAO.getByID(dto.stationId());
        User user = userDAO.getByID(dto.userCreatedById());

        user.ensureIsKitchenStaff();

        Set<Allergen> allergens = dto.allergenIds().stream()
            .map(allergenDAO::getByID)
            .collect(Collectors.toSet());

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

        DishSuggestion dish = dishSuggestionDAO.getByID(dishId);
        User approver = userDAO.getByID(approverId);

        dish.approve(approver);
        DishSuggestion updated = dishSuggestionDAO.update(dish);

        return mapToDTO(updated);
    }

    public DishSuggestionDTO rejectDish(Long dishId, Long approverId, String feedback)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(approverId);

        DishSuggestion dish = dishSuggestionDAO.getByID(dishId);
        User approver = userDAO.getByID(approverId);

        dish.reject(approver, feedback);
        DishSuggestion updated = dishSuggestionDAO.update(dish);

        return mapToDTO(updated);
    }

    public DishSuggestionDTO updateDish(DishUpdateRequestDTO dto)
    {
        ValidationUtil.validateId(dto.id());

        Optional<DishSuggestion> dish = dishSuggestionDAO.getByIdWithAllergens(dto.id());
        if(dish.isEmpty())
        {
            throw new EntityNotFoundException("Dish was not found");
        }
        User editor = userDAO.getByID(dto.editorId());

        editor.ensureIsKitchenStaff();

        Set<Allergen> allergens = dto.allergenIds().stream()
            .map(allergenDAO::getByID)
            .collect(Collectors.toSet());

        dish.get().updateContent(
            dto.nameDA(),
            dto.nameEN(),
            dto.descriptionDA(),
            dto.descriptionEN(),
            allergens,
            editor
        );

        DishSuggestion updated = dishSuggestionDAO.update(dish.get());

        return mapToDTO(updated);
    }

    public boolean deleteDish(Long dishId, Long userId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(userId);

        User user = userDAO.getByID(userId);
        DishSuggestion dish = dishSuggestionDAO.getByID(dishId);

        boolean isCreator = dish.getCreatedBy().getId().equals(userId);

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

    public DishSuggestionDTO getByIdWithAllergens(Long id)
    {
        ValidationUtil.validateId(id);

        Optional<DishSuggestion> dish = dishSuggestionDAO.getByIdWithAllergens(id);
        if(dish.isEmpty())
        {
            throw new EntityNotFoundException("Dish was not found");
        }

        return mapToDTO(dish.get());
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

    private DishSuggestionDTO mapToDTO(DishSuggestion dish)
    {
        return new DishSuggestionDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getNameEN(),
            dish.getDescriptionDA(),
            dish.getDescriptionEN(),
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
