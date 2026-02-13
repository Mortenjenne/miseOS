package app.dtos;

import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.enums.UserRole;

public record UserDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    UserRole userRole,
    Station station
) {

    public UserDTO(User user) {
        this(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getUserRole(),
            user.getStation()
        );
    }
}
