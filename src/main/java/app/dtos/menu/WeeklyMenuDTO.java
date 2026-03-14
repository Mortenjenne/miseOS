package app.dtos.menu;

import app.dtos.user.UserReferenceDTO;
import app.enums.MenuStatus;

import java.time.LocalDateTime;
import java.util.List;

public record WeeklyMenuDTO(
    Long menuId,
    int weekNumber,
    int year,
    MenuStatus menuStatus,
    LocalDateTime publishedAt,
    UserReferenceDTO menuCreatedBy,
    List<WeeklyMenuSlotDTO> menuSlots,
    int numberOfSlots
)
{
}
