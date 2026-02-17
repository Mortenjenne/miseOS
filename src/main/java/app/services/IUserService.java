package app.services;

import app.dtos.user.CreateUserRequestDTO;
import app.dtos.user.LoginRequestDTO;
import app.dtos.user.UserDTO;

import java.util.Set;

public interface IUserService
{
    UserDTO register(CreateUserRequestDTO request);

    UserDTO findById(Long id);

    Set<UserDTO> findAll();

    UserDTO login(LoginRequestDTO loginRequest);

    UserDTO update(UserDTO updateDTO);

    boolean delete(Long id);
}
