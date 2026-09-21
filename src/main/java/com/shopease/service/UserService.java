package com.shopease.service;

import com.shopease.dao.UserDAO;
import com.shopease.dto.UserDTO;
import com.shopease.exception.AuthenticationException;
import com.shopease.exception.ConflictException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.exception.ValidationException;
import com.shopease.factory.DaoFactory;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.PasswordUtil;
import com.shopease.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service handling user registration, authentication, and user profile queries.
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this(DaoFactory.getUserDAO());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new user account with role BUYER or SELLER.
     * Public registration cannot create ADMIN users.
     */
    public UserDTO register(String name, String email, String password, Role role) {
        ValidationUtil.requireNonBlank(name, "Full Name");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePassword(password);

        if (role == null) {
            role = Role.BUYER;
        }

        if (role == Role.ADMIN) {
            throw new ValidationException("Admin accounts cannot be registered publicly.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> existing = userDAO.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            logger.warn("Registration conflict: email already exists: {}", normalizedEmail);
            throw new ConflictException("An account with email '" + normalizedEmail + "' already exists.");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        User user = new User(null, name.trim(), normalizedEmail, hashedPassword, role, LocalDateTime.now());
        User created = userDAO.create(user);
        logger.info("Successfully registered new user ID={}, email={}, role={}", created.getId(), created.getEmail(), created.getRole());

        return UserDTO.fromEntity(created);
    }

    /**
     * Authenticates user credentials and returns user entity for session population.
     */
    public User authenticate(String email, String password) {
        ValidationUtil.validateEmail(email);
        ValidationUtil.requireNonBlank(password, "Password");

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userDAO.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: User not found for email {}", normalizedEmail);
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            logger.warn("Authentication failed: Incorrect password for email {}", normalizedEmail);
            throw new AuthenticationException("Invalid email or password.");
        }

        logger.info("User authenticated successfully: ID={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());
        return user;
    }

    public UserDTO findById(Long id) {
        if (id == null) {
            throw new ValidationException("User ID is required.");
        }
        User user = userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return UserDTO.fromEntity(user);
    }

    public Optional<User> findEntityById(Long id) {
        return userDAO.findById(id);
    }

    public List<UserDTO> findAllUsers() {
        return userDAO.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findUsersByRole(Role role) {
        return userDAO.findByRole(role).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public int countUsers() {
        return userDAO.countUsers();
    }
}
