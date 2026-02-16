package app.services;

import app.dtos.CreateUserRequest;
import app.dtos.LoginRequest;
import app.dtos.UserDTO;

import java.util.Set;

public interface IUserService
{
    UserDTO register(CreateUserRequest request);

    UserDTO findById(Long id);

    Set<UserDTO> findAll();

    UserDTO login(LoginRequest loginRequest);

    UserDTO update(UserDTO updateDTO);

    boolean delete(Long id);
}
