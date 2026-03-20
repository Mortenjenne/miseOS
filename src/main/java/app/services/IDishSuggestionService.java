package app.services;

import app.dtos.dishsuggestion.DishSuggestionCreateDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.dishsuggestion.DishSuggestionFilterDTO;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
import app.dtos.security.AuthenticatedUser;
import app.enums.Status;

import java.util.List;

public interface IDishSuggestionService
{
    DishSuggestionDTO createSuggestion(AuthenticatedUser authUser, DishSuggestionCreateDTO dto);

    DishSuggestionDTO approveSuggestion(AuthenticatedUser authUser, Long dishId);

    DishSuggestionDTO rejectSuggestion(AuthenticatedUser authUser, Long dishId, String feedback);

    DishSuggestionDTO updateSuggestion(AuthenticatedUser authUser, Long suggestionId, DishSuggestionUpdateDTO dto);

    boolean deleteSuggestion(AuthenticatedUser authUser, Long dishId);

    DishSuggestionDTO getById(AuthenticatedUser authUser, Long id);

    List<DishSuggestionDTO> getByFilter(AuthenticatedUser authUser, DishSuggestionFilterDTO dto);

    List<DishSuggestionDTO> getCurrentWeek(Status status);

    DishSuggestionDTO removeAllergen(AuthenticatedUser authUser, Long suggestionId, Long allergenId);
}
