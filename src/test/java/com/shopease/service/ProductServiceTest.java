package com.shopease.service;

import com.shopease.dao.ProductDAO;
import com.shopease.dao.ReviewDAO;
import com.shopease.dto.ProductDTO;
import com.shopease.exception.AuthorizationException;
import com.shopease.exception.ValidationException;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
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
public class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    @Mock
    private ReviewDAO reviewDAO;

    private ProductService productService;
    private User seller;
    private User otherSeller;
    private User buyer;

    @BeforeEach
    public void setUp() {
        productService = new ProductService(productDAO, reviewDAO);
        seller = new User(10L, "Seller One", "seller1@shopease.com", "pw", Role.SELLER, null);
        otherSeller = new User(20L, "Seller Two", "seller2@shopease.com", "pw", Role.SELLER, null);
        buyer = new User(30L, "Buyer", "buyer@shopease.com", "pw", Role.BUYER, null);
    }

    @Test
    public void testCreateProductSuccess() {
        when(productDAO.create(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(500L);
            return p;
        });

        ProductDTO dto = productService.createProduct(seller, "Basmati Rice", "Premium", "Grocery",
                new BigDecimal("499.00"), new BigDecimal("600.00"), 50, "rice.jpg");

        assertNotNull(dto);
        assertEquals(500L, dto.getId());
        assertEquals("Basmati Rice", dto.getName());
    }

    @Test
    public void testBuyerCannotCreateProduct() {
        assertThrows(AuthorizationException.class, () ->
                productService.createProduct(buyer, "Product", "Desc", "Grocery", new BigDecimal("100.00"), null, 10, null));
    }

    @Test
    public void testSellingPriceHigherThanOriginalPriceThrowsValidation() {
        assertThrows(ValidationException.class, () ->
                productService.createProduct(seller, "Product", "Desc", "Grocery", new BigDecimal("500.00"), new BigDecimal("400.00"), 10, null));
    }

    @Test
    public void testSellerCannotModifyOtherSellerProduct() {
        Product existing = new Product(1L, otherSeller.getId(), "Other Prod", "Desc", "Grocery", new BigDecimal("100.00"), null, null, 10, null, null, null);
        when(productDAO.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(AuthorizationException.class, () ->
                productService.updateProduct(seller, 1L, "New Name", "Desc", "Grocery", new BigDecimal("100.00"), null, 10, null));
    }
}
