package app.daos;

import app.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO
{
    User create(User user);

    Optional<User> findById(int id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    User update(User user);

    boolean delete(int id);
}
