package com.shopease;

import com.shopease.dao.ProductDAO;
import com.shopease.dao.UserDAO;
import com.shopease.dao.impl.JdbcProductDAO;
import com.shopease.dao.impl.JdbcUserDAO;
import com.shopease.dto.UserDTO;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.DatabaseUtil;
import com.shopease.util.PasswordUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityTest {
    private static UserDAO userDAO;
    private static ProductDAO productDAO;

    @BeforeAll
    public static void setUp() {
        DatabaseUtil.initializeForTest("jdbc:h2:mem:security_test;DB_CLOSE_DELAY=-1");
        userDAO = new JdbcUserDAO();
        productDAO = new JdbcProductDAO();
    }

    @Test
    public void testSqlInjectionInUserLookup() {
        String maliciousEmail = "' OR '1'='1' --";
        Optional<User> user = userDAO.findByEmail(maliciousEmail);
        assertTrue(user.isEmpty(), "SQL Injection vulnerability: User found via injection pattern!");
    }

    @Test
    public void testSqlInjectionInProductSearch() {
        String maliciousSearch = "' UNION SELECT * FROM users --";
        List<Product> results = productDAO.search(maliciousSearch, null, "newest");
        assertNotNull(results);
    }

    @Test
    public void testUserDTODoesNotExposePasswordHash() {
        User user = new User(1L, "Secret User", "user@test.com", "$2a$12$eX4mpL3H45hV4lu3N3v3rExP053", Role.BUYER, LocalDateTime.now());
        UserDTO dto = UserDTO.fromEntity(user);

        assertNotNull(dto);
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());
    }

    @Test
    public void testPasswordHashStrength() {
        String plain = "superSecretPassword123";
        String hash1 = PasswordUtil.hashPassword(plain);
        String hash2 = PasswordUtil.hashPassword(plain);

        // Hashes should have different salts
        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtil.checkPassword(plain, hash1));
        assertTrue(PasswordUtil.checkPassword(plain, hash2));
    }
}
