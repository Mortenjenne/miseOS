package app.dtos.menu;

import app.enums.MenuStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record WeeklyMenuOverviewDTO(
    Long id,
    int weekNumber,
    int year,
    MenuStatus menuStatus,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime publishedAt
) {}
