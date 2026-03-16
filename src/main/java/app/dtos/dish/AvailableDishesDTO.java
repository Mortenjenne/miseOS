package app.dtos.dish;

import java.util.List;
import java.util.Map;

public record AvailableDishesDTO(
    int week,
    int year,
    Map<String, List<DishOptionDTO>> thisWeekDishes,
    Map<String, List<DishOptionDTO>> fromDishBank
)
{
}
