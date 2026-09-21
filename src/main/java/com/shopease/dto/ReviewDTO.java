package com.shopease.dto;

import com.shopease.model.Review;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Product Reviews.
 */
public class ReviewDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private Long orderId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewDTO() {
    }

    public static ReviewDTO fromEntity(Review r) {
        if (r == null) {
            return null;
        }
        ReviewDTO dto = new ReviewDTO();
        dto.setId(r.getId());
        dto.setProductId(r.getProductId());
        dto.setUserId(r.getUserId());
        dto.setUserName(r.getUserName());
        dto.setOrderId(r.getOrderId());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
