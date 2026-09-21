package com.shopease.service;

import com.shopease.dao.OrderDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dao.ReviewDAO;
import com.shopease.dto.ReviewDTO;
import com.shopease.exception.AuthorizationException;
import com.shopease.exception.ConflictException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.exception.ValidationException;
import com.shopease.factory.DaoFactory;
import com.shopease.model.Order;
import com.shopease.model.OrderItem;
import com.shopease.model.OrderStatus;
import com.shopease.model.Review;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing verified product reviews and star ratings.
 * Strictly verifies order ownership, delivery status, and purchase history.
 */
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewDAO reviewDAO;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public ReviewService() {
        this(DaoFactory.getReviewDAO(), DaoFactory.getOrderDAO(), DaoFactory.getProductDAO());
    }

    public ReviewService(ReviewDAO reviewDAO, OrderDAO orderDAO, ProductDAO productDAO) {
        this.reviewDAO = reviewDAO;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    /**
     * Submits a new product review and rating from a verified buyer.
     */
    public ReviewDTO addReview(User buyer, Long productId, Long orderId, int rating, String comment) {
        if (buyer == null) {
            throw new AuthorizationException("You must be logged in to review a product.");
        }
        if (productId == null || orderId == null) {
            throw new ValidationException("Product ID and Order ID are required.");
        }
        ValidationUtil.validateRating(rating);
        ValidationUtil.requireNonBlank(comment, "Review Comment");

        if (comment.trim().length() > 1000) {
            throw new ValidationException("Review comment cannot exceed 1000 characters.");
        }

        // 1. Verify Product exists
        productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // 2. Verify Order exists and Buyer owns the order
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getBuyerId().equals(buyer.getId())) {
            logger.warn("Security violation: Buyer #{} tried to review using Order #{} owned by Buyer #{}",
                    buyer.getId(), orderId, order.getBuyerId());
            throw new AuthorizationException("You can only review products from your own orders.");
        }

        // 3. Verify Order status is DELIVERED
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ValidationException("Products can only be reviewed after order delivery. Current order status: " + order.getStatus());
        }

        // 4. Verify the product was actually part of that order
        List<OrderItem> items = orderDAO.findItemsByOrderId(orderId);
        boolean productInOrder = items.stream().anyMatch(i -> i.getProductId().equals(productId));
        if (!productInOrder) {
            throw new ValidationException("This product was not part of Order #" + orderId + ".");
        }

        // 5. Prevent duplicate review for same user + product + order
        if (reviewDAO.existsByBuyerAndProductAndOrder(buyer.getId(), productId, orderId)) {
            throw new ConflictException("You have already submitted a review for this product on Order #" + orderId + ".");
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(buyer.getId());
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setCreatedAt(LocalDateTime.now());

        Review created = reviewDAO.create(review);
        created.setUserName(buyer.getName());
        logger.info("Buyer #{} created {}-star review on Product #{}", buyer.getId(), rating, productId);

        return ReviewDTO.fromEntity(created);
    }

    public List<ReviewDTO> getReviewsByProduct(Long productId) {
        if (productId == null) {
            throw new ValidationException("Product ID is required.");
        }
        return reviewDAO.findByProductId(productId).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReviewDTO> getReviewsByUser(Long userId) {
        if (userId == null) {
            throw new ValidationException("User ID is required.");
        }
        return reviewDAO.findByUserId(userId).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void deleteReview(User user, Long reviewId) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Only administrators can moderate/delete reviews.");
        }
        reviewDAO.delete(reviewId);
        logger.info("Admin #{} deleted review #{}", user.getId(), reviewId);
    }

    public Double getAverageRating(Long productId) {
        return reviewDAO.getAverageRatingForProduct(productId);
    }

    public int getReviewCount(Long productId) {
        return reviewDAO.getReviewCountForProduct(productId);
    }
}
