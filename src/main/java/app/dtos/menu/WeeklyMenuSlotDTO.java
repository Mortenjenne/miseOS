package app.dtos.menu;

import app.dtos.dish.DishDTO;
import app.dtos.station.StationDTO;

import java.time.DayOfWeek;

public record WeeklyMenuSlotDTO(
    Long menuSlotId,
    DayOfWeek dayOfWeek,
    StationDTO stationDTO,
    DishDTO dishDTO
)
{
}
