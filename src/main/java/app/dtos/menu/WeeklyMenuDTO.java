package app.dtos.menu;

import app.dtos.user.UserReferenceDTO;
import app.enums.MenuStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record WeeklyMenuDTO(
    Long menuId,
    int weekNumber,
    int year,
    MenuStatus menuStatus,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime publishedAt,
    UserReferenceDTO menuCreatedBy,
    List<WeeklyMenuSlotDTO> menuSlots,
    int numberOfSlots
)
{
}
