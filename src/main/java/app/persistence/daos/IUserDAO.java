package app.persistence.daos;

import app.enums.UserRole;
import app.persistence.entities.User;

import java.util.Optional;
import java.util.Set;

public interface IUserDAO extends IUserReader, IEntityDAO<User, Long>
{
    Set<User> findByRole(UserRole role);
}
