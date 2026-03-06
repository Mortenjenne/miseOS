package app.services.impl;

import app.dtos.user.*;
import app.enums.UserRole;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.mappers.UserMapper;
import app.persistence.daos.interfaces.IStationReader;
import app.persistence.daos.interfaces.IUserDAO;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.services.IUserService;
import app.utils.PasswordUtil;
import app.utils.ValidationUtil;


import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UserService implements IUserService
{
    private final IUserDAO userDAO;
    private final IStationReader stationReader;

public UserService(IUserDAO userDAO, IStationReader stationReader)
    {
        this.userDAO = userDAO;
        this.stationReader = stationReader;
    }

    @Override
    public UserDTO registerUser(CreateUserRequestDTO dto)
    {
        validateCreateInput(dto);
        requireUniqueEmail(dto.email());

        String hashedPassword = PasswordUtil.hashPassword(dto.password());

        User user = new User(
            dto.firstName(),
            dto.lastName(),
            dto.email(),
            hashedPassword,
            UserRole.LINE_COOK
        );

        User created = userDAO.create(user);
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

        return userDAO.findByEmail(loginRequest.email())
            .filter(user -> user.verifyPassword(loginRequest.password()))
            .map(UserMapper::toDTO)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    }

    @Override
    public UserDTO update(Long userId, UpdateUserDTO dto)
    {
        ValidationUtil.validateId(userId);
        ValidationUtil.validateId(dto.stationId());
        requireMinimumLength(dto.firstName(), "First name");
        requireMinimumLength(dto.lastName(), "Last name");

        User user = userDAO.getByID(userId);
        Station station = stationReader.getByID(dto.stationId());

        user.update(
            dto.firstName(),
            dto.lastName(),
            station
        );

        User updated = userDAO.update(user);
        return UserMapper.toDTO(updated);
    }

    @Override
    public UserDTO assignToStation(Long requesterId, Long targetUserId, Long stationId) {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(targetUserId);
        ValidationUtil.validateId(stationId);

        User requester = userDAO.getByID(requesterId);
        requireHeadChef(requester);

        User targetUser = userDAO.getByID(targetUserId);
        Station station = stationReader.getByID(stationId);

        targetUser.assignToStation(station);

        User updated = userDAO.update(targetUser);
        return UserMapper.toDTO(updated);
    }

    @Override
    public UserDTO changeRole(Long requesterId, Long targetUserId, UserRole newRole)
    {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(targetUserId);
        ValidationUtil.validateNotNull(newRole, "Role");

        User requester = userDAO.getByID(requesterId);
        requireHeadChef(requester);

        User target = userDAO.getByID(targetUserId);
        target.changeRole(newRole);

        User updated = userDAO.update(target);
        return UserMapper.toDTO(updated);
    }

    @Override
    public UserDTO changeEmail(Long userId, String newEmail)
    {
        ValidationUtil.validateId(userId);
        ValidationUtil.validateEmail(newEmail);
        requireUniqueEmail(newEmail);

        User user = userDAO.getByID(userId);
        user.changeEmail(newEmail);

        User updated = userDAO.update(user);
        return UserMapper.toDTO(updated);
    }

    @Override
    public UserDTO changePassword(Long userId, ChangeUserPasswordDTO dto)
    {
        ValidationUtil.validateId(userId);
        validatePassword(dto.newPassword());

        User user = userDAO.getByID(userId);

        if (!user.verifyPassword(dto.currentPassword()))
        {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String hashed = PasswordUtil.hashPassword(dto.newPassword());
        user.changePassword(hashed);

        User updated = userDAO.update(user);
        return UserMapper.toDTO(updated);
    }

    @Override
    public boolean delete(Long requesterId, Long targetUserId)
    {
        ValidationUtil.validateId(requesterId);
        ValidationUtil.validateId(targetUserId);

        User requester = userDAO.getByID(requesterId);
        requireHeadChef(requester);

        if (requesterId.equals(targetUserId))
        {
            throw new IllegalArgumentException("Cannot delete your own account");
        }

        return userDAO.delete(targetUserId);
    }

    private void requireHeadChef(User user)
    {
        if (!user.isHeadChef())
        {
            throw new UnauthorizedActionException("Only head chef can change roles");
        }
    }

    private void validateCreateInput(CreateUserRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "User");
        requireMinimumLength(dto.firstName(), "First name");
        requireMinimumLength(dto.lastName(), "Last name");
        validatePassword(dto.password());
    }

    private void requireUniqueEmail(String email)
    {
        Optional<User> user = userDAO.findByEmail(email);

        if (user.isPresent())
        {
            throw new ValidationException("A user with this email already exists");
        }
    }

    private void validatePassword(String password)
    {
        if (password == null || password.length() < 8)
        {
            throw new ValidationException("Password must be at least 8 characters");
        }

        if (!password.matches(".*[A-Z].*"))
        {
            throw new ValidationException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[0-9].*"))
        {
            throw new ValidationException("Password must contain at least one number");
        }
    }

    private void requireMinimumLength(String name, String field)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException(field + " cannot be empty");
        }

        if (name.length() < 2)
        {
            throw new ValidationException(field + " must be at least 2 characters");
        }
    }
}


