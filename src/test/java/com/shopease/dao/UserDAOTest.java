package com.shopease.dao;

import com.shopease.dao.impl.JdbcUserDAO;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.DatabaseUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {
    private static UserDAO userDAO;

    @BeforeAll
    public static void setUp() {
        DatabaseUtil.initializeForTest("jdbc:h2:mem:user_dao_test;DB_CLOSE_DELAY=-1");
        userDAO = new JdbcUserDAO();
    }

    @Test
    public void testCreateAndFindUser() {
        String email = "testuser" + System.currentTimeMillis() + "@test.com";
        User user = new User(null, "Test User", email, "hashed_pw", Role.BUYER, LocalDateTime.now());

        User created = userDAO.create(user);
        assertNotNull(created.getId());

        Optional<User> foundById = userDAO.findById(created.getId());
        assertTrue(foundById.isPresent());
        assertEquals("Test User", foundById.get().getName());
        assertEquals(email.toLowerCase(), foundById.get().getEmail());

        Optional<User> foundByEmail = userDAO.findByEmail(email.toUpperCase());
        assertTrue(foundByEmail.isPresent());
        assertEquals(created.getId(), foundByEmail.get().getId());
    }

    @Test
    public void testFindByRole() {
        String email = "seller" + System.currentTimeMillis() + "@test.com";
        User seller = new User(null, "Test Seller", email, "hashed_pw", Role.SELLER, LocalDateTime.now());
        userDAO.create(seller);

        List<User> sellers = userDAO.findByRole(Role.SELLER);
        assertFalse(sellers.isEmpty());
        assertTrue(sellers.stream().anyMatch(s -> s.getEmail().equals(email)));
    }
}
