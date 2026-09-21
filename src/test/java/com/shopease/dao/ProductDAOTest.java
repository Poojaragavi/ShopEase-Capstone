package com.shopease.dao;

import com.shopease.dao.impl.JdbcProductDAO;
import com.shopease.dao.impl.JdbcUserDAO;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.DatabaseUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDAOTest {
    private static ProductDAO productDAO;
    private static Long sellerId;

    @BeforeAll
    public static void setUp() {
        DatabaseUtil.initializeForTest("jdbc:h2:mem:product_dao_test;DB_CLOSE_DELAY=-1");
        productDAO = new JdbcProductDAO();
        UserDAO userDAO = new JdbcUserDAO();

        User seller = new User(null, "Prod Seller", "prodseller" + System.currentTimeMillis() + "@test.com", "pw", Role.SELLER, LocalDateTime.now());
        User createdSeller = userDAO.create(seller);
        sellerId = createdSeller.getId();
    }

    @Test
    public void testCreateAndSearchProduct() {
        Product p = new Product(null, sellerId, "Organic Green Tea (100g)", "Pure green tea leaves", "Grocery",
                new BigDecimal("150.00"), new BigDecimal("200.00"), null, 25, "tea.png", null, null);

        Product created = productDAO.create(p);
        assertNotNull(created.getId());
        assertEquals(new BigDecimal("25.00"), created.getDiscountPercentage());

        Optional<Product> found = productDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Organic Green Tea (100g)", found.get().getName());

        List<Product> searchResults = productDAO.search("Green Tea", "Grocery", "newest");
        assertFalse(searchResults.isEmpty());

        // Test stock decrement
        boolean decremented = productDAO.decrementStock(created.getId(), 5);
        assertTrue(decremented);

        Product updated = productDAO.findById(created.getId()).orElseThrow();
        assertEquals(20, updated.getStockQty());
    }
}
