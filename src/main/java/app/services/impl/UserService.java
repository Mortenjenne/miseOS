package app.services.impl;

import app.dtos.CreateUserRequest;
import app.dtos.LoginRequest;
import app.dtos.UserDTO;
import app.enums.UserRole;
import app.persistence.daos.IUserDAO;
import app.persistence.entities.User;
import app.services.IUserService;
import app.utils.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;

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
    public UserDTO register(CreateUserRequest request)
    {
        validateUserRegistration(request);

        User userToSave = new User(
            request.firstName(),
            request.lastName(),
            request.email(),
            request.password(),
            UserRole.LINE_COOK
        );

        User userFromDB = userDAO.create(userToSave);


        return new UserDTO(userFromDB);
    }

    @Override
    public UserDTO findById(Long id)
    {
        User user = userDAO.getByID(id);
        if(user == null)
        {
          throw new EntityNotFoundException("User not found");
        }
        return new UserDTO(user);
    }

    @Override
    public Set<UserDTO> findAll()
    {
        return userDAO.getAll().stream()
            .map(UserDTO::new)
            .collect(Collectors.toSet());
    }


    @Override
    public UserDTO login(LoginRequest loginRequest)
    {
        ValidationUtil.validatePassword(loginRequest.password());
        ValidationUtil.validateEmail(loginRequest.email());

        return userDAO.findByEmail(loginRequest.email())
            .filter(user -> user.verifyPassword(loginRequest.password()))
            .map(UserDTO::new)
            .orElseThrow(() -> new IllegalArgumentException("Ugyldig mail eller password"));
    }

    @Override
    public UserDTO update(UserDTO updateDTO)
    {
        ValidationUtil.validateName(updateDTO.firstName(), "Fornavn");
        ValidationUtil.validateName(updateDTO.lastName(), "Efternavn");
        ValidationUtil.validateEmail(updateDTO.email());

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

        return new UserDTO(userDAO.update(updatedUser));
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


