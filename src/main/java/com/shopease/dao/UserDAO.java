package com.shopease.dao;

import com.shopease.model.Role;
import com.shopease.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User operations.
 */
public interface UserDAO {
    User create(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(Role role);
    boolean update(User user);
    boolean delete(Long id);
    int countUsers();
}
