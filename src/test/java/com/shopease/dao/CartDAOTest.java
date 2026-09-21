package com.shopease.dao;

import com.shopease.dao.impl.JdbcCartDAO;
import com.shopease.dao.impl.JdbcProductDAO;
import com.shopease.dao.impl.JdbcUserDAO;
import com.shopease.model.CartItem;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.DatabaseUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartDAOTest {
    private static CartDAO cartDAO;
    private static Long userId;
    private static Long productId;

    @BeforeAll
    public static void setUp() {
        DatabaseUtil.initializeForTest("jdbc:h2:mem:cart_dao_test;DB_CLOSE_DELAY=-1");
        cartDAO = new JdbcCartDAO();
        UserDAO userDAO = new JdbcUserDAO();
        ProductDAO productDAO = new JdbcProductDAO();

        User buyer = userDAO.create(new User(null, "Cart Buyer", "cartbuyer" + System.currentTimeMillis() + "@test.com", "pw", Role.BUYER, LocalDateTime.now()));
        userId = buyer.getId();

        User seller = userDAO.create(new User(null, "Cart Seller", "cartseller" + System.currentTimeMillis() + "@test.com", "pw", Role.SELLER, LocalDateTime.now()));
        Product product = productDAO.create(new Product(null, seller.getId(), "Cart Cookie", "Desc", "Snacks", new BigDecimal("50.00"), null, null, 10, null, null, null));
        productId = product.getId();
    }

    @Test
    public void testCartOperations() {
        CartItem item = new CartItem(null, userId, productId, 2, false, null);
        CartItem saved = cartDAO.saveOrUpdate(item);
        assertNotNull(saved.getId());

        List<CartItem> active = cartDAO.findByUserId(userId, false);
        assertEquals(1, active.size());
        assertEquals(2, active.get(0).getQuantity());

        // Update quantity
        cartDAO.updateQuantity(saved.getId(), 4);
        int count = cartDAO.countActiveCartItems(userId);
        assertEquals(4, count);

        // Save for later
        cartDAO.updateSavedForLater(saved.getId(), true);
        List<CartItem> savedList = cartDAO.findByUserId(userId, true);
        assertEquals(1, savedList.size());

        // Move back to cart
        cartDAO.updateSavedForLater(saved.getId(), false);
        assertEquals(1, cartDAO.findByUserId(userId, false).size());

        // Clear active cart
        cartDAO.clearActiveCart(userId);
        assertEquals(0, cartDAO.findByUserId(userId, false).size());
    }
}
