package app.services;

import app.dtos.ingredient.ApproveIngredientRequestDTO;
import app.dtos.ingredient.CreateIngredientRequestDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.ingredient.UpdateIngredientRequestDTO;
import app.dtos.security.AuthenticatedUser;
import app.enums.RequestType;
import app.enums.Status;

import java.time.LocalDate;
import java.util.List;

public interface IIngredientRequestService
{
    IngredientRequestDTO createIngredientRequest(AuthenticatedUser authUser, CreateIngredientRequestDTO requestDTO);

    IngredientRequestDTO approveIngredientRequest(AuthenticatedUser authUser, Long ingredientRequestId, ApproveIngredientRequestDTO approveDTO);

    IngredientRequestDTO rejectIngredientRequest(AuthenticatedUser authUser, Long requestId);

    List<IngredientRequestDTO> getRequests(AuthenticatedUser authUser, Status status, LocalDate deliveryDate, RequestType requestType, Long stationId);

    IngredientRequestDTO getById(AuthenticatedUser authUser, Long id);

    IngredientRequestDTO updateRequest(AuthenticatedUser authUser, Long ingredientRequestId, UpdateIngredientRequestDTO dto);

    boolean deleteRequest(AuthenticatedUser authUser, Long ingredientRequestId);
}
