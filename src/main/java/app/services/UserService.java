package app.services;

import app.dtos.CreateUserRequest;
import app.dtos.LoginRequest;
import app.dtos.UserDTO;

import java.util.List;

public interface UserService
{
    UserDTO register(CreateUserRequest request, String plainPassword);

    UserDTO findById(int id);

    List<UserDTO> findAll();

    UserDTO login(LoginRequest loginRequest);

    UserDTO update(UserDTO updateDTO);

    void delete(int id);
}
