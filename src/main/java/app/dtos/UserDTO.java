package app.dtos;

import app.entities.User;
import app.enums.Role;
import app.enums.Station;

public record UserDTO(
    int id,
    String firstName,
    String lastName,
    String email,
    Role role,
    Station station
) {

    public UserDTO(User user) {
        this(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole(),
            user.getStation()
        );
    }
}
