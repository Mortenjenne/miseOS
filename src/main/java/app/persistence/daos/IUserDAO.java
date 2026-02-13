package app.persistence.daos;

import app.persistence.entities.User;

import java.util.List;
import java.util.Optional;

public interface IUserDAO
{
    User create(User user);

    Optional<User> findById(int id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    User update(User user);

    boolean delete(int id);
}
