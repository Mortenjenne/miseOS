package app.dtos.menu;

import app.enums.DayOfWeek;

public record AddMenuSlotDTO(
    DayOfWeek dayOfWeek,
    Long stationId,
    Long dishId
)
{
}
