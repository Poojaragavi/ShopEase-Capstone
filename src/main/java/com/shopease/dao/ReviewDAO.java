package com.shopease.dao;

import com.shopease.model.Review;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Product Reviews.
 */
public interface ReviewDAO {
    Review create(Review review);
    Optional<Review> findById(Long id);
    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);
    boolean existsByBuyerAndProductAndOrder(Long userId, Long productId, Long orderId);
    Double getAverageRatingForProduct(Long productId);
    int getReviewCountForProduct(Long productId);
    boolean delete(Long id);
}
