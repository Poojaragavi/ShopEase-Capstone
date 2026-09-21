package com.shopease.service;

import com.shopease.dao.CartDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dto.CartDTO;
import com.shopease.dto.CartItemDTO;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.exception.ValidationException;
import com.shopease.factory.DaoFactory;
import com.shopease.model.CartItem;
import com.shopease.model.Product;
import com.shopease.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing shopping cart operations, quantity adjustments, and save-for-later items.
 */
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService() {
        this(DaoFactory.getCartDAO(), DaoFactory.getProductDAO());
    }

    public CartService(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    public CartItemDTO addToCart(Long userId, Long productId, int quantity) {
        if (userId == null) {
            throw new ValidationException("User must be logged in to manage cart.");
        }
        if (productId == null) {
            throw new ValidationException("Product ID is required.");
        }
        ValidationUtil.validateQuantity(quantity);

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getStockQty() <= 0) {
            throw new ValidationException("Sorry, '" + product.getName() + "' is currently out of stock.");
        }

        // Check current cart quantity
        Optional<CartItem> existing = cartDAO.findByUserIdAndProductId(userId, productId);
        int targetQty = quantity;
        if (existing.isPresent() && !existing.get().getSavedForLater()) {
            targetQty = existing.get().getQuantity() + quantity;
        }

        if (targetQty > product.getStockQty()) {
            throw new ValidationException("Requested total quantity (" + targetQty + ") exceeds available stock (" + product.getStockQty() + ").");
        }

        CartItem item = new CartItem(null, userId, productId, targetQty, false, null);
        item.setProduct(product);
        CartItem saved = cartDAO.saveOrUpdate(item);
        saved.setProduct(product);

        logger.info("Added product #{} to cart for user #{}, quantity={}", productId, userId, targetQty);
        return CartItemDTO.fromEntity(saved);
    }

    public CartItemDTO updateQuantity(Long userId, Long cartItemId, int quantity) {
        if (userId == null || cartItemId == null) {
            throw new ValidationException("Invalid user or cart item reference.");
        }
        ValidationUtil.validateQuantity(quantity);

        CartItem item = cartDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (!item.getUserId().equals(userId)) {
            throw new ValidationException("Cart item does not belong to the current user.");
        }

        Product product = productDAO.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product no longer exists."));

        if (quantity > product.getStockQty()) {
            throw new ValidationException("Cannot set quantity to " + quantity + ". Only " + product.getStockQty() + " items available in stock.");
        }

        cartDAO.updateQuantity(cartItemId, quantity);
        item.setQuantity(quantity);
        item.setProduct(product);

        logger.info("Updated cart item #{} quantity to {}", cartItemId, quantity);
        return CartItemDTO.fromEntity(item);
    }

    public void removeFromCart(Long userId, Long cartItemId) {
        if (userId == null || cartItemId == null) {
            throw new ValidationException("Invalid cart item reference.");
        }
        CartItem item = cartDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (!item.getUserId().equals(userId)) {
            throw new ValidationException("Unauthorized cart operation.");
        }

        cartDAO.delete(cartItemId);
        logger.info("Removed cart item #{} for user #{}", cartItemId, userId);
    }

    public void saveForLater(Long userId, Long cartItemId) {
        if (userId == null || cartItemId == null) {
            throw new ValidationException("Invalid cart item reference.");
        }
        CartItem item = cartDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found."));
        if (!item.getUserId().equals(userId)) {
            throw new ValidationException("Unauthorized cart operation.");
        }

        cartDAO.updateSavedForLater(cartItemId, true);
        logger.info("Saved item #{} for later for user #{}", cartItemId, userId);
    }

    public void moveToCart(Long userId, Long cartItemId) {
        if (userId == null || cartItemId == null) {
            throw new ValidationException("Invalid cart item reference.");
        }
        CartItem item = cartDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved item not found."));
        if (!item.getUserId().equals(userId)) {
            throw new ValidationException("Unauthorized cart operation.");
        }

        Product product = productDAO.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product no longer exists."));

        if (product.getStockQty() <= 0) {
            throw new ValidationException("Cannot move to cart: '" + product.getName() + "' is currently out of stock.");
        }

        cartDAO.updateSavedForLater(cartItemId, false);
        logger.info("Moved saved item #{} to active cart for user #{}", cartItemId, userId);
    }

    public CartDTO getCart(Long userId) {
        if (userId == null) {
            return new CartDTO();
        }

        List<CartItemDTO> activeItems = cartDAO.findByUserId(userId, false).stream()
                .map(CartItemDTO::fromEntity)
                .collect(Collectors.toList());

        List<CartItemDTO> savedItems = cartDAO.findByUserId(userId, true).stream()
                .map(CartItemDTO::fromEntity)
                .collect(Collectors.toList());

        CartDTO cart = new CartDTO();
        cart.setItems(activeItems);
        cart.setSavedForLaterItems(savedItems);
        cart.calculateTotals();
        return cart;
    }

    public int getCartItemCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return cartDAO.countActiveCartItems(userId);
    }

    public void clearActiveCart(Long userId) {
        if (userId != null) {
            cartDAO.clearActiveCart(userId);
        }
    }
}
