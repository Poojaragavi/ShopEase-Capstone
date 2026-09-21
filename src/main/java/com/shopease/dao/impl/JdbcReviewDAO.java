package com.shopease.dao.impl;

import com.shopease.dao.ReviewDAO;
import com.shopease.exception.DatabaseException;
import com.shopease.model.Review;
import com.shopease.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC implementation of ReviewDAO.
 */
public class JdbcReviewDAO implements ReviewDAO {
    private static final Logger logger = LoggerFactory.getLogger(JdbcReviewDAO.class);

    private static final String SELECT_BASE =
            "SELECT r.id, r.product_id, r.user_id, u.name AS user_name, r.order_id, r.rating, r.comment, r.created_at " +
            "FROM reviews r JOIN users u ON r.user_id = u.id ";

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (product_id, user_id, order_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setLong(3, review.getOrderId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setTimestamp(6, now);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Creating review failed.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    review.setId(rs.getLong(1));
                    review.setCreatedAt(now.toLocalDateTime());
                    return review;
                } else {
                    throw new DatabaseException("Creating review failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating review", e);
            throw new DatabaseException("Error creating review", e);
        }
    }

    @Override
    public Optional<Review> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE r.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToReview(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding review by ID: {}", id, e);
            throw new DatabaseException("Error finding review by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Review> findByProductId(Long productId) {
        List<Review> list = new ArrayList<>();
        if (productId == null) {
            return list;
        }
        String sql = SELECT_BASE + "WHERE r.product_id = ? ORDER BY r.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReview(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding reviews for product: {}", productId, e);
            throw new DatabaseException("Error finding reviews for product", e);
        }
        return list;
    }

    @Override
    public List<Review> findByUserId(Long userId) {
        List<Review> list = new ArrayList<>();
        if (userId == null) {
            return list;
        }
        String sql = SELECT_BASE + "WHERE r.user_id = ? ORDER BY r.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReview(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding reviews for user: {}", userId, e);
            throw new DatabaseException("Error finding reviews for user", e);
        }
        return list;
    }

    @Override
    public boolean existsByBuyerAndProductAndOrder(Long userId, Long productId, Long orderId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND product_id = ? AND order_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setLong(3, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking review existence", e);
            throw new DatabaseException("Error checking review existence", e);
        }
        return false;
    }

    @Override
    public Double getAverageRatingForProduct(Long productId) {
        String sql = "SELECT AVG(CAST(rating AS DOUBLE)) FROM reviews WHERE product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : Math.round(avg * 10.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating average rating for product: {}", productId, e);
            throw new DatabaseException("Error calculating average rating", e);
        }
        return 0.0;
    }

    @Override
    public int getReviewCountForProduct(Long productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting reviews for product: {}", productId, e);
            throw new DatabaseException("Error counting reviews", e);
        }
        return 0;
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting review: {}", id, e);
            throw new DatabaseException("Error deleting review", e);
        }
    }

    private Review mapRowToReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getLong("id"));
        r.setProductId(rs.getLong("product_id"));
        r.setUserId(rs.getLong("user_id"));
        r.setUserName(rs.getString("user_name"));
        r.setOrderId(rs.getLong("order_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            r.setCreatedAt(ts.toLocalDateTime());
        }
        return r;
    }
}
