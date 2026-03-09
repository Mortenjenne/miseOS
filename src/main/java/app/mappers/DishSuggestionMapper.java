package app.mappers;

import app.dtos.allergen.AllergenDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.station.StationReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.DishSuggestion;

import java.util.List;

public class DishSuggestionMapper
{

    private DishSuggestionMapper() {}

    public static DishSuggestionDTO toDTO(DishSuggestion suggestion)
    {
        List<AllergenDTO> allergens = suggestion.getAllergens()
            .stream()
            .map(AllergenMapper::toDTO)
            .toList();

        StationReferenceDTO station = StationMapper.toReferenceDTO(suggestion.getStation());
        UserReferenceDTO createdBy = UserMapper.toReferenceDTO(suggestion.getCreatedBy());
        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(suggestion.getReviewedBy());

        return new DishSuggestionDTO(
            suggestion.getId(),
            suggestion.getNameDA(),
            suggestion.getDescriptionDA(),
            suggestion.getDishStatus(),
            suggestion.getFeedback(),
            station,
            createdBy,
            reviewedBy,
            suggestion.getReviewedAt(),
            allergens,
            suggestion.getTargetWeek(),
            suggestion.getTargetYear(),
            suggestion.getCreatedAt()
        );
    }
}
