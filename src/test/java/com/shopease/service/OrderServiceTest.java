package com.shopease.service;

import com.shopease.dao.CartDAO;
import com.shopease.dao.OrderDAO;
import com.shopease.dao.PaymentDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dto.OrderDTO;
import com.shopease.exception.ValidationException;
import com.shopease.model.CartItem;
import com.shopease.model.Order;
import com.shopease.model.OrderStatus;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.payment.MockPaymentStrategy;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;
    @Mock
    private CartDAO cartDAO;
    @Mock
    private ProductDAO productDAO;
    @Mock
    private PaymentDAO paymentDAO;

    private PaymentService paymentService;
    private OrderService orderService;
    private User buyer;

    @BeforeEach
    public void setUp() {
        paymentService = new PaymentService(paymentDAO, new MockPaymentStrategy());
        orderService = new OrderService(orderDAO, cartDAO, productDAO, paymentDAO, paymentService);
        buyer = new User(1L, "Rahul", "rahul@test.com", "pw", Role.BUYER, LocalDateTime.now());
    }

    @Test
    public void testCheckoutSuccess() {
        Product p = new Product(10L, 2L, "Kulfi", "Desc", "Ice Cream", new BigDecimal("100.00"), null, null, 20, null, null, null);
        CartItem item = new CartItem(1L, 1L, 10L, 2, false, null);
        item.setProduct(p);

        when(cartDAO.findByUserId(1L, false)).thenReturn(List.of(item));
        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(productDAO.decrementStock(10L, 2)).thenReturn(true);
        when(orderDAO.createOrder(any(Order.class), anyList())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(999L);
            return o;
        });

        OrderDTO order = orderService.checkout(buyer, "Rahul", "9876543210", "Main St", "Delhi", "110001");

        assertNotNull(order);
        assertEquals(999L, order.getId());
        assertEquals(new BigDecimal("200.00"), order.getTotalAmount());
        assertNotNull(order.getTransactionId());

        verify(cartDAO).clearActiveCart(1L);
    }

    @Test
    public void testCheckoutEmptyCartFails() {
        when(cartDAO.findByUserId(1L, false)).thenReturn(List.of());

        assertThrows(ValidationException.class, () ->
                orderService.checkout(buyer, "Rahul", "9876543210", "Main St", "Delhi", "110001"));
    }
}
