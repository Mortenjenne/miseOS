package app.services;

import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.enums.Status;
import app.persistence.entities.User;

import java.time.LocalDate;
import java.util.Set;

public interface IIngredientRequestService
{
    IngredientRequestDTO createRequest(Long creatorId, CreateIngredientRequestDTO requestDTO);

    IngredientRequestDTO approveIngredientRequest(Long headChefId, Long ingredientRequestId);

    IngredientRequestDTO rejectIngredientRequest(Long headChefId, Long requestId);

    Set<IngredientRequestDTO> getAllPendingRequests();

    Set<IngredientRequestDTO> getRequestByStatus(Status status);

    Set<IngredientRequestDTO> getRequestByStatusAndDate(Status status, LocalDate deliveryDate);

    IngredientRequestDTO getById(Long id);

    IngredientRequestDTO updateRequest(Long editorId, Long ingredientRequestId, UpdateIngredientRequestDTO dto);

    boolean deleteRequest(Long requesterId, Long ingredientRequestId);
}
