package app.dtos.dish;

import java.util.Set;

public record DishSuggestionCreateDTO(String nameDA,
                                      String descriptionDA,
                                      Long stationId,
                                      Set<Long> allergenIds,
                                      Integer targetWeek,
                                      Integer targetYear)
{}
