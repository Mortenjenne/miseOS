package app.services;

import app.dtos.dishsuggestion.DishSuggestionCreateDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.dishsuggestion.DishSuggestionUpdateDTO;
import app.enums.Status;

import java.util.Set;

public interface IDishSuggestionService
{
    DishSuggestionDTO submitSuggestion(Long creatorId, DishSuggestionCreateDTO dto);

    DishSuggestionDTO approveDish(Long dishId, Long approverId);

    DishSuggestionDTO rejectDish(Long dishId, Long approverId, String feedback);

    DishSuggestionDTO updateDish(Long editorId, Long suggestionId, DishSuggestionUpdateDTO dto);

    boolean deleteDish(Long dishId, Long userId);

    DishSuggestionDTO getById(Long id);

    DishSuggestionDTO getByIdWithAllergens(Long id);

    Set<DishSuggestionDTO> getAllDishSuggestions();

    Set<DishSuggestionDTO> getPendingSuggestions();

    Set<DishSuggestionDTO> getPendingForWeek(int week, int year);

    Set<DishSuggestionDTO> getApprovedForWeek(int week, int year);

    Set<DishSuggestionDTO> getByStatus(Status status);
}
