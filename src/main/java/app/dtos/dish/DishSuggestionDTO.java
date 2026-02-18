package app.dtos.dish;

import app.enums.Status;

import java.util.Set;

public record DishSuggestionDTO(Long id,
                                String nameDA,
                                String nameEN,
                                String descriptionDA,
                                String descriptionEN,
                                Status dishStatus,
                                String feedback,
                                String stationName,
                                String createdByUsername,
                                Set<String> allergenNames,
                                Integer targetWeek,
                                Integer targetYear
)
{}
