package com.shopease.dao.impl;

import com.shopease.dao.CartDAO;
import com.shopease.exception.DatabaseException;
import com.shopease.model.CartItem;
import com.shopease.model.Product;
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
 * JDBC implementation of CartDAO.
 */
public class JdbcCartDAO implements CartDAO {
    private static final Logger logger = LoggerFactory.getLogger(JdbcCartDAO.class);

    private static final String SELECT_BASE =
            "SELECT c.id, c.user_id, c.product_id, c.quantity, c.saved_for_later, c.created_at, " +
            "p.seller_id, p.name AS product_name, p.description AS product_desc, p.category AS product_category, " +
            "p.price AS product_price, p.original_price AS product_orig_price, p.discount_percentage, " +
            "p.stock_qty, p.image_url " +
            "FROM cart_items c JOIN products p ON c.product_id = p.id ";

    @Override
    public CartItem saveOrUpdate(CartItem cartItem) {
        // First check if already exists for this user and product
        Optional<CartItem> existing = findByUserIdAndProductId(cartItem.getUserId(), cartItem.getProductId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            // If it was saved for later and user adds to cart, move to active cart
            boolean newSaved = cartItem.getSavedForLater() != null ? cartItem.getSavedForLater() : item.getSavedForLater();
            int newQty = cartItem.getQuantity();

            String sql = "UPDATE cart_items SET quantity = ?, saved_for_later = ? WHERE id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newQty);
                ps.setBoolean(2, newSaved);
                ps.setLong(3, item.getId());
                ps.executeUpdate();
                item.setQuantity(newQty);
                item.setSavedForLater(newSaved);
                return item;
            } catch (SQLException e) {
                logger.error("Error updating cart item: {}", item.getId(), e);
                throw new DatabaseException("Error updating cart item", e);
            }
        } else {
            String sql = "INSERT INTO cart_items (user_id, product_id, quantity, saved_for_later, created_at) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, cartItem.getUserId());
                ps.setLong(2, cartItem.getProductId());
                ps.setInt(3, cartItem.getQuantity() != null ? cartItem.getQuantity() : 1);
                ps.setBoolean(4, cartItem.getSavedForLater());
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                ps.setTimestamp(5, now);

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new DatabaseException("Creating cart item failed.");
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        cartItem.setId(rs.getLong(1));
                        cartItem.setCreatedAt(now.toLocalDateTime());
                        return cartItem;
                    } else {
                        throw new DatabaseException("Creating cart item failed, no ID obtained.");
                    }
                }
            } catch (SQLException e) {
                logger.error("Error creating cart item", e);
                throw new DatabaseException("Error creating cart item", e);
            }
        }
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE c.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCartItem(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cart item by ID: {}", id, e);
            throw new DatabaseException("Error finding cart item by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE c.user_id = ? AND c.product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCartItem(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cart item by user {} and product {}", userId, productId, e);
            throw new DatabaseException("Error finding cart item", e);
        }
        return Optional.empty();
    }

    @Override
    public List<CartItem> findByUserId(Long userId, boolean savedForLater) {
        List<CartItem> list = new ArrayList<>();
        if (userId == null) {
            return list;
        }
        String sql = SELECT_BASE + "WHERE c.user_id = ? AND c.saved_for_later = ? ORDER BY c.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setBoolean(2, savedForLater);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToCartItem(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cart items for user: {}", userId, e);
            throw new DatabaseException("Error finding cart items", e);
        }
        return list;
    }

    @Override
    public boolean updateQuantity(Long cartItemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating cart quantity for item: {}", cartItemId, e);
            throw new DatabaseException("Error updating cart quantity", e);
        }
    }

    @Override
    public boolean updateSavedForLater(Long cartItemId, boolean savedForLater) {
        String sql = "UPDATE cart_items SET saved_for_later = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, savedForLater);
            ps.setLong(2, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating saved for later for item: {}", cartItemId, e);
            throw new DatabaseException("Error updating saved for later", e);
        }
    }

    @Override
    public boolean delete(Long cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting cart item: {}", cartItemId, e);
            throw new DatabaseException("Error deleting cart item", e);
        }
    }

    @Override
    public boolean deleteByUserIdAndProductId(Long userId, Long productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting cart item by user and product", e);
            throw new DatabaseException("Error deleting cart item", e);
        }
    }

    @Override
    public boolean clearActiveCart(Long userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND saved_for_later = FALSE";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Error clearing active cart for user: {}", userId, e);
            throw new DatabaseException("Error clearing cart", e);
        }
    }

    @Override
    public int countActiveCartItems(Long userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = ? AND saved_for_later = FALSE";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting cart items for user: {}", userId, e);
            throw new DatabaseException("Error counting cart items", e);
        }
        return 0;
    }

    private CartItem mapRowToCartItem(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setSavedForLater(rs.getBoolean("saved_for_later"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            item.setCreatedAt(ts.toLocalDateTime());
        }

        Product p = new Product();
        p.setId(rs.getLong("product_id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setName(rs.getString("product_name"));
        p.setDescription(rs.getString("product_desc"));
        p.setCategory(rs.getString("product_category"));
        p.setPrice(rs.getBigDecimal("product_price"));
        p.setOriginalPrice(rs.getBigDecimal("product_orig_price"));
        p.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setImageUrl(rs.getString("image_url"));

        item.setProduct(p);
        return item;
    }
}
