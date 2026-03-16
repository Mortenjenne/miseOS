package app.dtos.dishsuggestion;

import app.enums.Status;

public record DishSuggestionFilterDTO(
    Status status,
    Integer week,
    Integer year,
    Long    stationId,
    String  orderBy
)
{
}
