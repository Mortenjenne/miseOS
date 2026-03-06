package app.dtos.user;

import app.enums.UserRole;

public record UserDTO(
    String firstName,
    String lastName,
    String email,
    UserRole userRole,
    String stationName
)
{
}
