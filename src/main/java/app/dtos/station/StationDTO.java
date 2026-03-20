package app.dtos.station;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record StationDTO(
    Long id,
    String name,
    String description,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime updatedAt
)
{
}
