package app.services.impl;

import app.daos.UserDAO;
import app.dtos.CreateUserRequest;
import app.dtos.LoginRequest;
import app.dtos.UserDTO;
import app.entities.User;
import app.services.UserService;
import app.utils.PasswordUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public class UserServiceImpl implements UserService
{
    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO)
    {
        this.userDAO = userDAO;
    }

    @Override
    public UserDTO register(CreateUserRequest request, String plainPassword)
    {
        validateUserRegistration(request);

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        User userToSave = User.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .hashedPassword(hashedPassword)
            .role(request.role())
            .station(request.station())
            .build();

        User userFromDB = userDAO.create(userToSave);

        return new UserDTO(userFromDB);
    }

    @Override
    public UserDTO findById(int id)
    {
        User user = userDAO.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new UserDTO(user);
    }

    @Override
    public List<UserDTO> findAll()
    {
        return userDAO.findAll().stream()
            .map(UserDTO::new)
            .toList();
    }

    @Override
    public UserDTO login(LoginRequest loginRequest)
    {
        ValidationUtil.validateEmail(loginRequest.email());

        return userDAO.findByEmail(loginRequest.email())
            .filter(user -> PasswordUtil.verifyPassword(loginRequest.password(), user.getHashedPassword()))
            .map(UserDTO::new)
            .orElseThrow(() -> new IllegalArgumentException("Ugyldig mail eller password"));
    }

    @Override
    public UserDTO update(UserDTO updateDTO)
    {
        ValidationUtil.validateName(updateDTO.firstName(), "Fornavn");
        ValidationUtil.validateName(updateDTO.lastName(), "Efternavn");
        ValidationUtil.validateEmail(updateDTO.email());

        User existingUser = userDAO.findById(updateDTO.id())
            .orElseThrow(() -> new IllegalArgumentException("Brugeren med ID " + updateDTO.id() + " findes ikke"));

        User updatedUser = existingUser.toBuilder()
            .firstName(updateDTO.firstName())
            .lastName(updateDTO.lastName())
            .email(updateDTO.email())
            .role(updateDTO.role())
            .station(updateDTO.station())
            .build();

        return new UserDTO(userDAO.update(updatedUser));
    }

    @Override
    public void delete(int id)
    {
        if (!userDAO.findById(id).isPresent())
        {
            throw new IllegalArgumentException("Kan ikke slette: Bruger med ID " + id + " findes ikke");
        }

        userDAO.delete(id);
    }

    private void validateUserRegistration(CreateUserRequest request)
    {
        ValidationUtil.validateName(request.firstName(), "Fornavn");
        ValidationUtil.validateName(request.lastName(), "Efternavn");
        ValidationUtil.validateEmail(request.email());
        ValidationUtil.validatePassword(request.password());

        if (userDAO.findByEmail(request.email()).isPresent())
        {
            throw new IllegalArgumentException("En bruger med denne email findes allerede");
        }
    }
}
