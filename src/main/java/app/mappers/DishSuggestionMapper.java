package app.mappers;

import app.dtos.allergen.AllergenDTO;
import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.dtos.station.StationReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.Allergen;
import app.persistence.entities.DishSuggestion;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DishSuggestionMapper
{

    private DishSuggestionMapper() {}

    public static DishSuggestionDTO toDTO(DishSuggestion suggestion)
    {
        List<AllergenDTO> allergens = suggestion.getAllergens()
            .stream()
            .map(AllergenMapper::toDTO)
            .toList();

        StationReferenceDTO station = new StationReferenceDTO(
            suggestion.getStation().getId(),
            suggestion.getStation().getStationName()
        );

        UserReferenceDTO createdBy = new UserReferenceDTO(
            suggestion.getCreatedBy().getId(),
            suggestion.getCreatedBy().getFirstName(),
            suggestion.getCreatedBy().getLastName()
        );

        UserReferenceDTO reviewedBy = suggestion.getReviewedBy() != null
            ? new UserReferenceDTO(
            suggestion.getReviewedBy().getId(),
            suggestion.getReviewedBy().getFirstName(),
            suggestion.getReviewedBy().getLastName())
            : null;

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
