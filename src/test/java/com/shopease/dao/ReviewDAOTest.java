package com.shopease.dao;

import com.shopease.dao.impl.JdbcOrderDAO;
import com.shopease.dao.impl.JdbcProductDAO;
import com.shopease.dao.impl.JdbcReviewDAO;
import com.shopease.dao.impl.JdbcUserDAO;
import com.shopease.model.Order;
import com.shopease.model.OrderItem;
import com.shopease.model.OrderStatus;
import com.shopease.model.Product;
import com.shopease.model.Review;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.DatabaseUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewDAOTest {
    private static ReviewDAO reviewDAO;
    private static Long buyerId;
    private static Long productId;
    private static Long orderId;

    @BeforeAll
    public static void setUp() {
        DatabaseUtil.initializeForTest("jdbc:h2:mem:review_dao_test;DB_CLOSE_DELAY=-1");
        reviewDAO = new JdbcReviewDAO();
        UserDAO userDAO = new JdbcUserDAO();
        ProductDAO productDAO = new JdbcProductDAO();
        OrderDAO orderDAO = new JdbcOrderDAO();

        User buyer = userDAO.create(new User(null, "Reviewer", "reviewer" + System.currentTimeMillis() + "@test.com", "pw", Role.BUYER, LocalDateTime.now()));
        buyerId = buyer.getId();

        User seller = userDAO.create(new User(null, "Seller", "seller" + System.currentTimeMillis() + "@test.com", "pw", Role.SELLER, LocalDateTime.now()));
        Product product = productDAO.create(new Product(null, seller.getId(), "Reviewed Prod", "Desc", "Fragrance", new BigDecimal("500.00"), null, null, 10, null, null, null));
        productId = product.getId();

        Order order = new Order(null, buyerId, OrderStatus.DELIVERED, new BigDecimal("500.00"), "Reviewer", "9876543210", "St", "City", "123456", LocalDateTime.now());
        List<OrderItem> items = List.of(new OrderItem(null, null, productId, 1, new BigDecimal("500.00")));
        Order createdOrder = orderDAO.createOrder(order, items);
        orderId = createdOrder.getId();
    }

    @Test
    public void testCreateAndQueryReviews() {
        Review r = new Review(null, productId, buyerId, orderId, 5, "Outstanding fragrance!", LocalDateTime.now());
        Review created = reviewDAO.create(r);
        assertNotNull(created.getId());

        assertTrue(reviewDAO.existsByBuyerAndProductAndOrder(buyerId, productId, orderId));
        assertEquals(5.0, reviewDAO.getAverageRatingForProduct(productId));
        assertEquals(1, reviewDAO.getReviewCountForProduct(productId));

        List<Review> list = reviewDAO.findByProductId(productId);
        assertEquals(1, list.size());
        assertEquals("Outstanding fragrance!", list.get(0).getComment());
    }
}
