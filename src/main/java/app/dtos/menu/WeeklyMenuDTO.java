package app.dtos.menu;

import app.enums.MenuStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record WeeklyMenuDTO(
    Long menuId,
    int weekNumber,
    int year,
    MenuStatus menuStatus,
    LocalDateTime publishedAt,
    String publishedBy,
    Set<WeeklyMenuSlotDTO> menuSlots
)
{
}
