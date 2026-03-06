package app.mappers;

import app.dtos.dishsuggestion.DishSuggestionDTO;
import app.persistence.entities.Allergen;
import app.persistence.entities.DishSuggestion;

import java.util.Set;
import java.util.stream.Collectors;

public class DishSuggestionMapper
{

    private DishSuggestionMapper() {}

    public static DishSuggestionDTO toDTO(DishSuggestion suggestion)
    {
        Set<String> allergenNames = suggestion.getAllergens().stream()
            .map(Allergen::getNameDA)
            .collect(Collectors.toSet());

        String chefName = suggestion.getCreatedBy().getFirstName() + " " + suggestion.getCreatedBy().getLastName();

        return new DishSuggestionDTO(
            suggestion.getId(),
            suggestion.getNameDA(),
            suggestion.getDescriptionDA(),
            suggestion.getDishStatus(),
            suggestion.getFeedback(),
            suggestion.getStation().getStationName(),
            chefName,
            allergenNames,
            suggestion.getTargetWeek(),
            suggestion.getTargetYear()
        );
    }
}
