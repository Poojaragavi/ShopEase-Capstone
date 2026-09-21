package com.shopease.service;

import com.shopease.dao.CartDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dto.CartItemDTO;
import com.shopease.exception.ValidationException;
import com.shopease.model.CartItem;
import com.shopease.model.Product;
import java.math.BigDecimal;
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
public class CartServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    public void setUp() {
        cartService = new CartService(cartDAO, productDAO);
    }

    @Test
    public void testAddToCartSuccess() {
        Product p = new Product(1L, 2L, "Chips", "Desc", "Snacks", new BigDecimal("40.00"), null, null, 10, null, null, null);
        when(productDAO.findById(1L)).thenReturn(Optional.of(p));
        when(cartDAO.findByUserIdAndProductId(100L, 1L)).thenReturn(Optional.empty());
        when(cartDAO.saveOrUpdate(any(CartItem.class))).thenAnswer(inv -> {
            CartItem item = inv.getArgument(0);
            item.setId(10L);
            return item;
        });

        CartItemDTO dto = cartService.addToCart(100L, 1L, 2);
        assertNotNull(dto);
        assertEquals(2, dto.getQuantity());
    }

    @Test
    public void testAddToCartExceedingStockThrowsValidation() {
        Product p = new Product(1L, 2L, "Chips", "Desc", "Snacks", new BigDecimal("40.00"), null, null, 5, null, null, null);
        when(productDAO.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(ValidationException.class, () ->
                cartService.addToCart(100L, 1L, 10));
    }
}
