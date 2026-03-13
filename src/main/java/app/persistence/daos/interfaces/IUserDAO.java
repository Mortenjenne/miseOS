package app.persistence.daos.interfaces;

import app.enums.UserRole;
import app.persistence.daos.interfaces.generic.IEntityDAO;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.User;

import java.util.Set;

public interface IUserDAO extends IUserReader, IEntityDAO<User, Long>
{
    Set<User> getAll();

    Set<User> findByRole(UserRole role);
}
