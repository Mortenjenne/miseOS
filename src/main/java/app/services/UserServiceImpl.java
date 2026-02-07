package app.services;

import app.daos.UserDAO;
import app.dtos.UserDTO;
import app.entities.User;

public class UserServiceImpl
{
    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO)
    {
        this.userDAO = userDAO;
    }

    public UserDTO create(User user)
    {
        return null;
    }
}
