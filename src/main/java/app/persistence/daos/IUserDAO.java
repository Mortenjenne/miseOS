package app.persistence.daos;

import app.enums.UserRole;
import app.persistence.entities.User;

import java.util.Optional;
import java.util.Set;

public interface IUserDAO extends IEntityDAO<User, Long>
{
    Optional<User> findByEmail(String email);
    Set<User> findByRole(UserRole role);
    boolean existsByEmail(String email);
}
