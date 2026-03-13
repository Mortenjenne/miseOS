package app.dtos.menu;

import app.dtos.dish.DishDTO;
import app.dtos.station.StationDTO;
import app.dtos.station.StationReferenceDTO;
import app.enums.DayOfWeek;


public record WeeklyMenuSlotDTO(
    Long menuSlotId,
    DayOfWeek dayOfWeek,
    StationReferenceDTO,
    DishDTO dishDTO
)
{
}
