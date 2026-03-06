package app.mappers;

import app.dtos.user.UserDTO;
import app.persistence.entities.User;

public class UserMapper
{
    private UserMapper() {}

    public static UserDTO toDTO(User user)
    {
        return new UserDTO(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getUserRole(),
            user.getStation() != null ? user.getStation().getId() : null,
            user.getStation() != null ? user.getStation().getStationName() : null,
            user.getCreatedAt()
        );
    }
}
