package app.services;

import app.dtos.dishsuggestion.DishSuggestionCreateDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
import app.enums.Status;

import java.util.Set;

public interface IDishSuggestionService
{
    DishSuggestionDTO createSuggestion(Long creatorId, DishSuggestionCreateDTO dto);

    DishSuggestionDTO approveSuggestion(Long dishId, Long approverId);

    DishSuggestionDTO rejectSuggestion(Long dishId, Long approverId, String feedback);

    DishSuggestionDTO updateSuggestion(Long editorId, Long suggestionId, DishSuggestionUpdateDTO dto);

    boolean deleteSuggestion(Long dishId, Long userId);

    DishSuggestionDTO getById(Long id);

    DishSuggestionDTO getByIdWithAllergens(Long id);

    Set<DishSuggestionDTO> getAllDishSuggestions();

    Set<DishSuggestionDTO> getPendingSuggestions();

    Set<DishSuggestionDTO> getPendingForWeek(int week, int year);

    Set<DishSuggestionDTO> getApprovedForWeek(int week, int year);

    Set<DishSuggestionDTO> getByStatus(Status status);
}
