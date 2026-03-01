package app.dtos.dish;

import java.util.Map;
import java.util.Set;

public record AvailableDishesDTO(
    int week,
    int year,
    Map<String, Set<DishOptionDTO>> thisWeekDishes,
    Map<String, Set<DishOptionDTO>> fromDishBank
)
{
}
