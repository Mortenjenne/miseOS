package app.dtos;

import app.enums.UserRole;
import app.persistence.entities.Station;

public record CreateUserRequest(
    String firstName,
    String lastName,
    String email,
    String password,
    UserRole userRole,
    Station station
) {}
