package app.services;

import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.enums.RequestType;
import app.enums.Status;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface IIngredientRequestService
{
    IngredientRequestDTO createIngredientRequest(Long creatorId, CreateIngredientRequestDTO requestDTO);

    IngredientRequestDTO approveIngredientRequest(Long headChefId, Long ingredientRequestId);

    IngredientRequestDTO rejectIngredientRequest(Long headChefId, Long requestId);

    List<IngredientRequestDTO> getRequests(Long userId, Status status, LocalDate deliveryDate, RequestType requestType);

    IngredientRequestDTO getById(Long id);

    IngredientRequestDTO updateRequest(Long editorId, Long ingredientRequestId, UpdateIngredientRequestDTO dto);

    boolean deleteRequest(Long requesterId, Long ingredientRequestId);
}
