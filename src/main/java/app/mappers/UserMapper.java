package app.mappers;

import app.dtos.station.StationReferenceDTO;
import app.dtos.user.UserDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.User;

public class UserMapper
{
    private UserMapper() {}

    public static UserDTO toDTO(User user)
    {
        StationReferenceDTO stationReferenceDTO = StationMapper.toReferenceDTO(user.getStation());

        return new UserDTO(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getUserRole(),
            stationReferenceDTO,
            user.getCreatedAt()
        );
    }

    public static UserReferenceDTO toReferenceDTO(User user)
    {
        if (user == null)
        {
            return null;
        }

        return new UserReferenceDTO(
            user.getId(),
            user.getFirstName(),
            user.getLastName()
        );
    }
}
