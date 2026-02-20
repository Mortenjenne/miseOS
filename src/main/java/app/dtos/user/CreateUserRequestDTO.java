package app.dtos.user;

import app.enums.UserRole;
import app.persistence.entities.Station;

public record CreateUserRequestDTO(
    String firstName,
    String lastName,
    String email,
    String password,
    UserRole userRole,
    Station station
) {}
