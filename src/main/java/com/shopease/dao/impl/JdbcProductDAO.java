package com.shopease.dao.impl;

import com.shopease.dao.ProductDAO;
import com.shopease.exception.DatabaseException;
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
 * JDBC implementation of ProductDAO.
 */
public class JdbcProductDAO implements ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(JdbcProductDAO.class);

    private static final String SELECT_BASE =
            "SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.category, " +
            "p.price, p.original_price, p.discount_percentage, p.stock_qty, p.image_url, " +
            "p.created_at, p.updated_at " +
            "FROM products p JOIN users u ON p.seller_id = u.id ";

    @Override
    public Product create(Product product) {
        String sql = "INSERT INTO products (seller_id, name, description, category, price, original_price, " +
                "discount_percentage, stock_qty, image_url, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            product.calculateDiscount();

            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setString(4, product.getCategory());
            ps.setBigDecimal(5, product.getPrice());
            ps.setBigDecimal(6, product.getOriginalPrice());
            ps.setBigDecimal(7, product.getDiscountPercentage());
            ps.setInt(8, product.getStockQty() != null ? product.getStockQty() : 0);
            ps.setString(9, product.getImageUrl());
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setTimestamp(10, now);
            ps.setTimestamp(11, now);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                    product.setCreatedAt(now.toLocalDateTime());
                    product.setUpdatedAt(now.toLocalDateTime());
                    return product;
                } else {
                    throw new DatabaseException("Creating product failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating product: {}", product.getName(), e);
            throw new DatabaseException("Error creating product", e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE p.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product by ID: {}", id, e);
            throw new DatabaseException("Error finding product by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY p.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all products", e);
            throw new DatabaseException("Error finding all products", e);
        }
        return list;
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) {
        List<Product> list = new ArrayList<>();
        if (sellerId == null) {
            return list;
        }
        String sql = SELECT_BASE + "WHERE p.seller_id = ? ORDER BY p.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding products by seller ID: {}", sellerId, e);
            throw new DatabaseException("Error finding products by seller ID", e);
        }
        return list;
    }

    @Override
    public List<Product> findByCategory(String category) {
        List<Product> list = new ArrayList<>();
        if (category == null || category.trim().isEmpty()) {
            return findAll();
        }
        String sql = SELECT_BASE + "WHERE LOWER(p.category) = LOWER(?) ORDER BY p.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding products by category: {}", category, e);
            throw new DatabaseException("Error finding products by category", e);
        }
        return list;
    }

    @Override
    public List<Product> search(String keyword, String category, String sortBy) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(p.category) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category.trim())) {
            sql.append("AND LOWER(p.category) = LOWER(?) ");
            params.add(category.trim());
        }

        // Sorting
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.price ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.price DESC ");
        } else if ("discount".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.discount_percentage DESC, p.id DESC ");
        } else {
            sql.append("ORDER BY p.id DESC ");
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching products: keyword={}, category={}", keyword, category, e);
            throw new DatabaseException("Error searching products", e);
        }
        return list;
    }

    @Override
    public List<String> findAllCategories() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL ORDER BY category ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            logger.error("Error finding all categories", e);
            throw new DatabaseException("Error finding all categories", e);
        }
        return list;
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, category = ?, price = ?, original_price = ?, " +
                "discount_percentage = ?, stock_qty = ?, image_url = ?, updated_at = ? WHERE id = ? AND seller_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            product.calculateDiscount();

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getCategory());
            ps.setBigDecimal(4, product.getPrice());
            ps.setBigDecimal(5, product.getOriginalPrice());
            ps.setBigDecimal(6, product.getDiscountPercentage());
            ps.setInt(7, product.getStockQty() != null ? product.getStockQty() : 0);
            ps.setString(8, product.getImageUrl());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(10, product.getId());
            ps.setLong(11, product.getSellerId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating product: {}", product.getId(), e);
            throw new DatabaseException("Error updating product", e);
        }
    }

    @Override
    public boolean updateStock(Long productId, int newStock) {
        String sql = "UPDATE products SET stock_qty = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating product stock: {}", productId, e);
            throw new DatabaseException("Error updating product stock", e);
        }
    }

    @Override
    public boolean decrementStock(Long productId, int quantity) {
        String sql = "UPDATE products SET stock_qty = stock_qty - ?, updated_at = ? WHERE id = ? AND stock_qty >= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, productId);
            ps.setInt(4, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error decrementing stock for product: {}", productId, e);
            throw new DatabaseException("Error decrementing stock", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting product: {}", id, e);
            throw new DatabaseException("Error deleting product", e);
        }
    }

    @Override
    public int countBySellerId(Long sellerId) {
        String sql = "SELECT COUNT(*) FROM products WHERE seller_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting products for seller: {}", sellerId, e);
            throw new DatabaseException("Error counting seller products", e);
        }
        return 0;
    }

    @Override
    public int countLowStockBySellerId(Long sellerId, int threshold) {
        String sql = "SELECT COUNT(*) FROM products WHERE seller_id = ? AND stock_qty > 0 AND stock_qty <= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            ps.setInt(2, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting low stock for seller: {}", sellerId, e);
            throw new DatabaseException("Error counting low stock products", e);
        }
        return 0;
    }

    @Override
    public int countOutOfStockBySellerId(Long sellerId) {
        String sql = "SELECT COUNT(*) FROM products WHERE seller_id = ? AND stock_qty = 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting out of stock for seller: {}", sellerId, e);
            throw new DatabaseException("Error counting out of stock products", e);
        }
        return 0;
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setSellerName(rs.getString("seller_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setOriginalPrice(rs.getBigDecimal("original_price"));
        p.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setImageUrl(rs.getString("image_url"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) {
            p.setCreatedAt(cTs.toLocalDateTime());
        }
        Timestamp uTs = rs.getTimestamp("updated_at");
        if (uTs != null) {
            p.setUpdatedAt(uTs.toLocalDateTime());
        }
        return p;
    }
}
