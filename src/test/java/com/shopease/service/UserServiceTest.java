package com.shopease.service;

import com.shopease.dao.UserDAO;
import com.shopease.dto.UserDTO;
import com.shopease.exception.AuthenticationException;
import com.shopease.exception.ConflictException;
import com.shopease.exception.ValidationException;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.PasswordUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService(userDAO);
    }

    @Test
    public void testSuccessfulRegistration() {
        when(userDAO.findByEmail("aditi@shopease.com")).thenReturn(Optional.empty());
        when(userDAO.create(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(101L);
            return u;
        });

        UserDTO dto = userService.register("Aditi Sharma", "aditi@shopease.com", "password123", Role.BUYER);
        assertNotNull(dto);
        assertEquals(101L, dto.getId());
        assertEquals("aditi@shopease.com", dto.getEmail());
        assertEquals(Role.BUYER, dto.getRole());
    }

    @Test
    public void testDuplicateEmailRegistrationThrowsConflict() {
        when(userDAO.findByEmail("aditi@shopease.com")).thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () ->
                userService.register("Aditi Sharma", "aditi@shopease.com", "password123", Role.BUYER));
    }

    @Test
    public void testAdminRegistrationBlocked() {
        assertThrows(ValidationException.class, () ->
                userService.register("Hacker", "admin@shopease.com", "pass123", Role.ADMIN));
    }

    @Test
    public void testAuthenticationSuccess() {
        String hash = PasswordUtil.hashPassword("secret123");
        User user = new User(1L, "Valid User", "user@shopease.com", hash, Role.BUYER, LocalDateTime.now());
        when(userDAO.findByEmail("user@shopease.com")).thenReturn(Optional.of(user));

        User authenticated = userService.authenticate("user@shopease.com", "secret123");
        assertNotNull(authenticated);
        assertEquals(1L, authenticated.getId());
    }

    @Test
    public void testAuthenticationFailureWrongPassword() {
        String hash = PasswordUtil.hashPassword("secret123");
        User user = new User(1L, "Valid User", "user@shopease.com", hash, Role.BUYER, LocalDateTime.now());
        when(userDAO.findByEmail("user@shopease.com")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () ->
                userService.authenticate("user@shopease.com", "wrongpassword"));
    }
}
