package app.dtos;

import app.enums.UserRole;

public record CreateUserRequest(
    String firstName,
    String lastName,
    String email,
    String password,
    UserRole userRole,
    Station station
) {}
