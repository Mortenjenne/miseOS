package app.services;

import app.dtos.dish.DishCreateRequestDTO;
import app.dtos.dish.DishSuggestionDTO;
import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.IDishSuggestionDAO;
import app.persistence.daos.IStationDAO;
import app.persistence.daos.IUserDAO;
import app.persistence.entities.Allergen;
import app.persistence.entities.DishSuggestion;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

public class DishSuggestionService
{
    private final IDishSuggestionDAO dishSuggestionDAO;
    private final IUserDAO userDAO;
    private final IStationDAO stationDAO;

    public DishSuggestionService(IDishSuggestionDAO dishSuggestionDAO, IUserDAO userDAO, IStationDAO stationDAO)
    {
        this.dishSuggestionDAO = dishSuggestionDAO;
        this.userDAO = userDAO;
        this.stationDAO = stationDAO;
    }

    public DishSuggestionDTO submitSuggestion(DishCreateRequestDTO createRequest)
    {
        ValidationUtil.validateNotBlank(createRequest.nameDA(), "Dish name");
        ValidationUtil.validateNotBlank(createRequest.descriptionDA(), "Dish description");
        ValidationUtil.validateId(createRequest.stationId());
        ValidationUtil.validateId(createRequest.userCreatedById());
        ValidationUtil.validateNotNull(createRequest.allergenIds(), "Allergies");

        Station station = stationDAO.getByID(createRequest.stationId());

        if(station == null)
        {
            throw new EntityNotFoundException("Station was not found.");
        }

        User user = userDAO.getByID(createRequest.userCreatedById());
        validateUserNotNull(user);
        validateCanCreateRequest(user);

        //TODO How should allergens be implemented!
        DishSuggestion request = new DishSuggestion(
            createRequest.nameDA(),
            createRequest.descriptionDA(),
            station,
            user
        );

        DishSuggestion dishSuggestion = dishSuggestionDAO.create(request);

        return mapToDTO(dishSuggestion);
    }

    public DishSuggestionDTO approveDish(Long dishId, Long chefId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(chefId);

        DishSuggestion dishSuggestion = dishSuggestionDAO.getByID(dishId);
        User chef = userDAO.getByID(chefId);



        dishSuggestion.approve(chef);

        DishSuggestion updated = dishSuggestionDAO.update(dishSuggestion);

        return mapToDTO(updated);
    }

    public DishSuggestion updateDish(DishSuggestionDTO dishSuggestionDTO)
    {
        return null;
    }

    public boolean deleteDish(Long dishId, Long chefId)
    {
        ValidationUtil.validateId(dishId);
        ValidationUtil.validateId(chefId);

        User chef = userDAO.getByID(chefId);
        DishSuggestion dishSuggestion = dishSuggestionDAO.getByID(dishId);

        validateUserNotNull(chef);
        validateIsHeadChef(chef);
        validateDishNotNull(dishSuggestion);

        return dishSuggestionDAO.delete(dishId);
    }

    public DishSuggestionDTO getById(Long id)
    {
        return null;
    }

    public DishSuggestionDTO getByIdWithAllergens(Long id)
    {
        return null;
    }

    public DishSuggestionDTO getAllDishSuggestions()
    {
        return null;
    }

    public Set<DishSuggestionDTO> getPendingSuggestions()
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByStatus(Status.PENDING);

        return dishes.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toSet());
    }

    //TODO needs status?
    public Set<DishSuggestionDTO> getWeeklyMenu(int week, int year)
    {
        Set<DishSuggestion> dishes = dishSuggestionDAO.findByWeekAndYear(week, year);

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

    //TODO implement dao?
    public Set<DishSuggestionDTO> getByStatusAndWeek(Status status, int week, int year)
    {
        return null;
    }

    private void validateIsHeadChef(User user)
    {
        if (!user.isHeadChef()) {
            throw new UnauthorizedActionException("Only head chefs can approve/reject requests");
        }
    }

    private void validateCanCreateRequest(User user)
    {
        if (!user.isLineCook() && !user.isHeadChef())
        {
            throw new UnauthorizedActionException("Only kitchen staff can create ingredient requests");
        }
    }

    private void validateUserNotNull(User user)
    {
        if(user == null)
        {
            throw new EntityNotFoundException("User was not found.");
        }
    }

    private void validateDishNotNull(DishSuggestion dishSuggestion)
    {
        if(dishSuggestion == null)
        {
            throw new EntityNotFoundException("Dish was not found.");
        }
    }

    private DishSuggestionDTO mapToDTO(DishSuggestion dishSuggestion)
    {
        return new DishSuggestionDTO(
            dishSuggestion.getId(),
            dishSuggestion.getNameDA(),
            dishSuggestion.getNameEN(),
            dishSuggestion.getDescriptionDA(),
            dishSuggestion.getDescriptionEN(),
            dishSuggestion.getDishStatus(),
            dishSuggestion.getFeedback(),
            dishSuggestion.getStation().getStationName(),
            dishSuggestion.getCreatedBy().getFirstName(),
            dishSuggestion.getAllergens().stream().map(Allergen::getName).collect(Collectors.toSet())
        );
    }
}
