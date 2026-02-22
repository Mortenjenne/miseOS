package app.services;

import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.enums.Status;
import app.persistence.entities.User;

import java.time.LocalDate;
import java.util.Set;

public interface IIngredientRequestService
{
    IngredientRequestDTO createRequest(User creator, CreateIngredientRequestDTO requestDTO);

    IngredientRequestDTO approveIngredientRequest(User headChef, Long ingredientRequestId);

    IngredientRequestDTO rejectIngredientRequest(User headChef, Long requestId);

    Set<IngredientRequestDTO> getAllPendingRequests();

    Set<IngredientRequestDTO> getRequestByStatus(Status status);

    Set<IngredientRequestDTO> getRequestByStatusAndDate(Status status, LocalDate deliveryDate);

    IngredientRequestDTO getById(Long id);

    IngredientRequestDTO updateRequest(User creator, IngredientRequestDTO requestDTO);

    boolean deleteRequest(Long id, User headChef);
}
