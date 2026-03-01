package app.persistence.daos.interfaces;

import app.persistence.entities.User;

import java.util.Optional;

public interface IUserReader extends IEntityReader<User, Long>
{
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
