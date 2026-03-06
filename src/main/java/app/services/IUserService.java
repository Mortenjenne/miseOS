package app.services;

import app.dtos.user.*;
import app.enums.UserRole;

import java.util.Set;

public interface IUserService
{
    UserDTO registerUser(CreateUserRequestDTO dto);

    UserDTO findById(Long id);

    Set<UserDTO> findAll();

    UserDTO login(LoginRequestDTO loginRequest);

    UserDTO update(Long id, UpdateUserDTO dto);

    boolean delete(Long requesterId, Long targetUserId);

    UserDTO changeRole(Long requesterId, Long targetUserId, UserRole newRole);

    UserDTO changeEmail(Long userId, String newEmail);

    UserDTO changePassword(Long userId, ChangeUserPasswordDTO dto);
}
