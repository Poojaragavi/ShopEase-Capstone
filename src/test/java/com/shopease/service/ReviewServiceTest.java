package com.shopease.service;

import com.shopease.dao.OrderDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dao.ReviewDAO;
import com.shopease.dto.ReviewDTO;
import com.shopease.exception.AuthorizationException;
import com.shopease.exception.ValidationException;
import com.shopease.model.Order;
import com.shopease.model.OrderItem;
import com.shopease.model.OrderStatus;
import com.shopease.model.Product;
import com.shopease.model.Review;
import com.shopease.model.Role;
import com.shopease.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;
    @Mock
    private OrderDAO orderDAO;
    @Mock
    private ProductDAO productDAO;

    private ReviewService reviewService;
    private User buyer;
    private User otherBuyer;

    @BeforeEach
    public void setUp() {
        reviewService = new ReviewService(reviewDAO, orderDAO, productDAO);
        buyer = new User(1L, "Buyer One", "b1@test.com", "pw", Role.BUYER, LocalDateTime.now());
        otherBuyer = new User(2L, "Buyer Two", "b2@test.com", "pw", Role.BUYER, LocalDateTime.now());
    }

    @Test
    public void testAddReviewSuccess() {
        Product p = new Product(10L, 5L, "Mango Juice", "Desc", "Juice", new BigDecimal("100.00"), null, null, 10, null, null, null);
        Order o = new Order(100L, buyer.getId(), OrderStatus.DELIVERED, new BigDecimal("100.00"), "Buyer", "123", "Addr", "City", "123456", LocalDateTime.now());
        OrderItem item = new OrderItem(1L, 100L, 10L, 1, new BigDecimal("100.00"));

        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(orderDAO.findById(100L)).thenReturn(Optional.of(o));
        when(orderDAO.findItemsByOrderId(100L)).thenReturn(List.of(item));
        when(reviewDAO.existsByBuyerAndProductAndOrder(1L, 10L, 100L)).thenReturn(false);
        when(reviewDAO.create(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });

        ReviewDTO dto = reviewService.addReview(buyer, 10L, 100L, 5, "Delicious fresh mango juice!");
        assertNotNull(dto);
        assertEquals(50L, dto.getId());
        assertEquals(5, dto.getRating());
    }

    @Test
    public void testReviewFailsIfNotDelivered() {
        Product p = new Product(10L, 5L, "Mango Juice", "Desc", "Juice", new BigDecimal("100.00"), null, null, 10, null, null, null);
        Order o = new Order(100L, buyer.getId(), OrderStatus.SHIPPED, new BigDecimal("100.00"), "Buyer", "123", "Addr", "City", "123456", LocalDateTime.now());

        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(orderDAO.findById(100L)).thenReturn(Optional.of(o));

        assertThrows(ValidationException.class, () ->
                reviewService.addReview(buyer, 10L, 100L, 5, "Good"));
    }

    @Test
    public void testReviewFailsIfNotOrderOwner() {
        Product p = new Product(10L, 5L, "Mango Juice", "Desc", "Juice", new BigDecimal("100.00"), null, null, 10, null, null, null);
        Order o = new Order(100L, otherBuyer.getId(), OrderStatus.DELIVERED, new BigDecimal("100.00"), "Other", "123", "Addr", "City", "123456", LocalDateTime.now());

        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(orderDAO.findById(100L)).thenReturn(Optional.of(o));

        assertThrows(AuthorizationException.class, () ->
                reviewService.addReview(buyer, 10L, 100L, 5, "Good"));
    }
}
