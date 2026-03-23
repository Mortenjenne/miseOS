package app.dtos.user;

import app.dtos.station.StationReferenceDTO;
import app.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    UserRole userRole,
    StationReferenceDTO station,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime createdAt
)
{
}
