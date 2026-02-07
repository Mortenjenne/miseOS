package app.dtos;

import app.enums.Role;
import app.enums.Station;

public record CreateUserRequest(
    String firstName,
    String lastName,
    String email,
    String password,
    Role role,
    Station station
) {}
