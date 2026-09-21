package com.shopease.dao;

import com.shopease.model.CartItem;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Cart operations.
 */
public interface CartDAO {
    CartItem saveOrUpdate(CartItem cartItem);
    Optional<CartItem> findById(Long id);
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    List<CartItem> findByUserId(Long userId, boolean savedForLater);
    boolean updateQuantity(Long cartItemId, int quantity);
    boolean updateSavedForLater(Long cartItemId, boolean savedForLater);
    boolean delete(Long cartItemId);
    boolean deleteByUserIdAndProductId(Long userId, Long productId);
    boolean clearActiveCart(Long userId);
    int countActiveCartItems(Long userId);
}
