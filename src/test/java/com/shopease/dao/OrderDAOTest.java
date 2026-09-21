package com.shopease.dao;

import com.shopease.dao.impl.JdbcOrderDAO;
import com.shopease.dao.impl.JdbcProductDAO;
import com.shopease.dao.impl.JdbcUserDAO;
import com.shopease.model.Order;
import com.shopease.model.OrderItem;
import com.shopease.model.OrderStatus;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.DatabaseUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderDAOTest {
    private static OrderDAO orderDAO;
    private static Long buyerId;
    private static Long sellerId;
    private static Long productId;

    @BeforeAll
    public static void setUp() {
        DatabaseUtil.initializeForTest("jdbc:h2:mem:order_dao_test;DB_CLOSE_DELAY=-1");
        orderDAO = new JdbcOrderDAO();
        UserDAO userDAO = new JdbcUserDAO();
        ProductDAO productDAO = new JdbcProductDAO();

        User buyer = userDAO.create(new User(null, "Order Buyer", "ordbuyer" + System.currentTimeMillis() + "@test.com", "pw", Role.BUYER, LocalDateTime.now()));
        buyerId = buyer.getId();

        User seller = userDAO.create(new User(null, "Order Seller", "ordseller" + System.currentTimeMillis() + "@test.com", "pw", Role.SELLER, LocalDateTime.now()));
        sellerId = seller.getId();

        Product product = productDAO.create(new Product(null, sellerId, "Order Item Prod", "Desc", "Juice", new BigDecimal("120.00"), null, null, 50, null, null, null));
        productId = product.getId();
    }

    @Test
    public void testCreateAndTrackOrder() {
        Order order = new Order(null, buyerId, OrderStatus.CONFIRMED, new BigDecimal("240.00"), "John Doe", "9876543210", "123 Main St", "Mumbai", "400001", LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem(null, null, productId, 2, new BigDecimal("120.00"));
        items.add(item);

        Order created = orderDAO.createOrder(order, items);
        assertNotNull(created.getId());

        Optional<Order> found = orderDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.CONFIRMED, found.get().getStatus());
        assertEquals(1, found.get().getItems().size());

        // Update status to DELIVERED
        orderDAO.updateStatus(created.getId(), OrderStatus.DELIVERED);
        assertTrue(orderDAO.isOrderDelivered(created.getId()));
        assertTrue(orderDAO.hasPurchasedProduct(buyerId, productId));

        // Seller revenue
        BigDecimal rev = orderDAO.calculateSellerRevenue(sellerId);
        assertTrue(rev.compareTo(BigDecimal.ZERO) > 0);
    }
}
