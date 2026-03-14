package app.services;

import app.dtos.dishsuggestion.DishSuggestionCreateDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.dishsuggestion.DishSuggestionFilterDTO;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
import app.enums.Status;

import java.util.List;

public interface IDishSuggestionService
{
    DishSuggestionDTO createSuggestion(Long creatorId, DishSuggestionCreateDTO dto);

    DishSuggestionDTO approveSuggestion(Long dishId, Long approverId);

    DishSuggestionDTO rejectSuggestion(Long dishId, Long approverId, String feedback);

    DishSuggestionDTO updateSuggestion(Long editorId, Long suggestionId, DishSuggestionUpdateDTO dto);

    boolean deleteSuggestion(Long dishId, Long userId);

    DishSuggestionDTO getById(Long id);

    List<DishSuggestionDTO> getByFilter(DishSuggestionFilterDTO dto);

    List<DishSuggestionDTO> getCurrentWeek(Status status);
}
