package app.services.impl;

import app.dtos.user.CreateUserRequestDTO;
import app.dtos.user.LoginRequestDTO;
import app.dtos.user.UserDTO;
import app.enums.UserRole;
import app.exceptions.ValidationException;
import app.mappers.UserMapper;
import app.persistence.daos.interfaces.IUserDAO;
import app.persistence.entities.User;
import app.services.IUserService;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UserService implements IUserService
{
    private final IUserDAO userDAO;

public UserService(IUserDAO userDAO)
    {
        this.userDAO = userDAO;
    }

    @Override
    public UserDTO registerUser(Long stationId, CreateUserRequestDTO dto)
    {
        ValidationUtil.validateId(stationId);
        validatePasswordAndEmail(dto.password(), dto.email());
        String firstName = validateName(dto.firstName(), "First name");
        String lastName = validateName(dto.lastName(), "Last name");

        User userToSave = new User(
            firstName,
            lastName,
            dto.email(),
            dto.password(),
            UserRole.LINE_COOK
        );

        User created = userDAO.create(userToSave);
        return UserMapper.toDTO(created);
    }

    @Override
    public UserDTO findById(Long id)
    {
        ValidationUtil.validateId(id);

        User user = userDAO.getByID(id);
        return UserMapper.toDTO(user);
    }

    @Override
    public Set<UserDTO> findAll()
    {
        return userDAO.getAll().stream()
            .map(UserMapper::toDTO)
            .collect(Collectors.toSet());
    }


    @Override
    public UserDTO login(LoginRequestDTO loginRequest)
    {
        validatePassword(loginRequest.password());
        validateEmail(loginRequest.email());

        return userDAO.findByEmail(loginRequest.email())
            .filter(user -> user.verifyPassword(loginRequest.password()))
            .map(UserMapper::toDTO)
            .orElseThrow(() -> new IllegalArgumentException("Ugyldig mail eller password"));
    }

    @Override
    public UserDTO update(UserDTO updateDTO)
    {
        String firstName = validateName(updateDTO.firstName(), "First name");
        String lastName = validateName(updateDTO.lastName(), "Last name");
        String email = ValidationUtil.validateEmail(updateDTO.email());

        User existingUser = userDAO.getByID(updateDTO.id());

        if(existingUser == null)
        {
            throw new EntityNotFoundException("Brugeren med ID " + updateDTO.id() + " findes ikke");
        }

        existingUser.setFirstName(updateDTO.firstName());
        existingUser.setLastName(updateDTO.lastName());
        existingUser.setEmail(updateDTO.email());
        existingUser.setUserRole(updateDTO.userRole());

        User updatedUser = userDAO.update(existingUser);
        return UserMapper.toDTO(updatedUser);
    }

    @Override
    public boolean delete(Long id)
    {
        User user = userDAO.getByID(id);
        if (user == null)
        {
            throw new EntityNotFoundException("Kan ikke slette: Bruger med ID " + id + " findes ikke");
        }

        return userDAO.delete(id);
    }

    private void validatePasswordAndEmail(String password, String email)
    {
        validatePassword(password);
        validateEmail(email);
    }

    private void validateEmail(String email)
    {
        ValidationUtil.validateEmail(email);
        Optional<User> user = userDAO.findByEmail(email);

        if (user.isPresent())
        {
            throw new ValidationException("En bruger med denne email findes allerede");
        }
    }

    private void validatePassword(String password)
    {
        if (password == null || password.length() < 8)
        {
            throw new ValidationException("Password skal være mindst 8 tegn");
        }

        if (!password.matches(".*[A-Z].*"))
        {
            throw new ValidationException("Password skal indeholde et stort bogstav");
        }

        if (!password.matches(".*[0-9].*"))
        {
            throw new ValidationException("Password skal indeholde et tal");
        }
    }

    private String validateName(String name, String fieldName)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " kan ikke være tomt");
        }

        if (name.length() < 2)
        {
            throw new ValidationException(fieldName + " skal være mindst 2 tegn");
        }
        return name.trim();
    }
}


