package app.dtos.menu;

import app.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record WeeklyMenuDTO(
    Long menuId,
    int weekNumber,
    int year,
    Status status,
    LocalDateTime publishedAt,
    String publishedBy,
    Set<WeeklyMenuSlotDTO> menuSlots
)
{
}
