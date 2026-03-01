package app.dtos.user;

import app.enums.UserRole;

public record UserDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    UserRole userRole,
    String stationName
)
{
}
