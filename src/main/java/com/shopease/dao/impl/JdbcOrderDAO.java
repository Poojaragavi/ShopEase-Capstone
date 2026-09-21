package com.shopease.dao.impl;

import com.shopease.dao.OrderDAO;
import com.shopease.exception.DatabaseException;
import com.shopease.model.Order;
import com.shopease.model.OrderItem;
import com.shopease.model.OrderStatus;
import com.shopease.util.DatabaseUtil;
import java.math.BigDecimal;
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
 * JDBC implementation of OrderDAO.
 */
public class JdbcOrderDAO implements OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(JdbcOrderDAO.class);

    @Override
    public Order createOrder(Order order, List<OrderItem> items) {
        String insertOrderSql = "INSERT INTO orders (buyer_id, status, total_amount, customer_name, phone, address, city, pincode, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psItem = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS)) {

                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                psOrder.setLong(1, order.getBuyerId());
                psOrder.setString(2, order.getStatus() != null ? order.getStatus().name() : OrderStatus.PENDING.name());
                psOrder.setBigDecimal(3, order.getTotalAmount());
                psOrder.setString(4, order.getCustomerName());
                psOrder.setString(5, order.getPhone());
                psOrder.setString(6, order.getAddress());
                psOrder.setString(7, order.getCity());
                psOrder.setString(8, order.getPincode());
                psOrder.setTimestamp(9, now);

                int affected = psOrder.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    throw new DatabaseException("Creating order failed, no rows affected.");
                }

                long orderId;
                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getLong(1);
                        order.setId(orderId);
                        order.setCreatedAt(now.toLocalDateTime());
                    } else {
                        conn.rollback();
                        throw new DatabaseException("Creating order failed, no ID obtained.");
                    }
                }

                for (OrderItem item : items) {
                    psItem.setLong(1, orderId);
                    psItem.setLong(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setBigDecimal(4, item.getUnitPrice());
                    psItem.addBatch();
                }

                psItem.executeBatch();
                conn.commit();
                order.setItems(items);
                return order;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction rollback during order creation", e);
                throw new DatabaseException("Error creating order: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            logger.error("Database error during order creation", e);
            throw new DatabaseException("Database error during order creation", e);
        }
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        String sql = "SELECT o.id, o.buyer_id, u.email AS buyer_email, o.status, o.total_amount, " +
                "o.customer_name, o.phone, o.address, o.city, o.pincode, o.created_at " +
                "FROM orders o JOIN users u ON o.buyer_id = u.id WHERE o.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapRowToOrder(rs);
                    order.setItems(findItemsByOrderId(orderId));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order by ID: {}", orderId, e);
            throw new DatabaseException("Error finding order by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        List<Order> list = new ArrayList<>();
        if (buyerId == null) {
            return list;
        }
        String sql = "SELECT o.id, o.buyer_id, u.email AS buyer_email, o.status, o.total_amount, " +
                "o.customer_name, o.phone, o.address, o.city, o.pincode, o.created_at " +
                "FROM orders o JOIN users u ON o.buyer_id = u.id WHERE o.buyer_id = ? ORDER BY o.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapRowToOrder(rs);
                    order.setItems(findItemsByOrderId(order.getId()));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding orders for buyer: {}", buyerId, e);
            throw new DatabaseException("Error finding orders for buyer", e);
        }
        return list;
    }

    @Override
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.id, o.buyer_id, u.email AS buyer_email, o.status, o.total_amount, " +
                "o.customer_name, o.phone, o.address, o.city, o.pincode, o.created_at " +
                "FROM orders o JOIN users u ON o.buyer_id = u.id ORDER BY o.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapRowToOrder(rs);
                order.setItems(findItemsByOrderId(order.getId()));
                list.add(order);
            }
        } catch (SQLException e) {
            logger.error("Error finding all orders", e);
            throw new DatabaseException("Error finding all orders", e);
        }
        return list;
    }

    @Override
    public List<Order> findBySellerId(Long sellerId) {
        List<Order> list = new ArrayList<>();
        if (sellerId == null) {
            return list;
        }
        String sql = "SELECT DISTINCT o.id, o.buyer_id, u.email AS buyer_email, o.status, o.total_amount, " +
                "o.customer_name, o.phone, o.address, o.city, o.pincode, o.created_at " +
                "FROM orders o " +
                "JOIN users u ON o.buyer_id = u.id " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ? " +
                "ORDER BY o.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapRowToOrder(rs);
                    order.setItems(findItemsByOrderId(order.getId()));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding orders for seller: {}", sellerId, e);
            throw new DatabaseException("Error finding orders for seller", e);
        }
        return list;
    }

    @Override
    public boolean updateStatus(Long orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating order status for ID: {}", orderId, e);
            throw new DatabaseException("Error updating order status", e);
        }
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, " +
                "p.name AS product_name, p.image_url AS product_image, p.seller_id " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setProductName(rs.getString("product_name"));
                    item.setProductImageUrl(rs.getString("product_image"));
                    item.setSellerId(rs.getLong("seller_id"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding items for order: {}", orderId, e);
            throw new DatabaseException("Error finding items for order", e);
        }
        return list;
    }

    @Override
    public boolean hasPurchasedProduct(Long buyerId, Long productId) {
        String sql = "SELECT COUNT(*) FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.buyer_id = ? AND oi.product_id = ? AND o.status = 'DELIVERED'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, buyerId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking purchase status", e);
            throw new DatabaseException("Error checking purchase status", e);
        }
        return false;
    }

    @Override
    public boolean isOrderDelivered(Long orderId) {
        String sql = "SELECT status FROM orders WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "DELIVERED".equalsIgnoreCase(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking order delivery status for ID: {}", orderId, e);
            throw new DatabaseException("Error checking order status", e);
        }
        return false;
    }

    @Override
    public int countOrdersBySellerId(Long sellerId) {
        String sql = "SELECT COUNT(DISTINCT o.id) FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting orders for seller: {}", sellerId, e);
            throw new DatabaseException("Error counting seller orders", e);
        }
        return 0;
    }

    @Override
    public BigDecimal calculateSellerRevenue(Long sellerId) {
        String sql = "SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0) " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ? AND o.status != 'CANCELLED'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating revenue for seller: {}", sellerId, e);
            throw new DatabaseException("Error calculating seller revenue", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public int countTotalOrders() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting total orders", e);
            throw new DatabaseException("Error counting total orders", e);
        }
        return 0;
    }

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setBuyerId(rs.getLong("buyer_id"));
        order.setBuyerEmail(rs.getString("buyer_email"));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setPhone(rs.getString("phone"));
        order.setAddress(rs.getString("address"));
        order.setCity(rs.getString("city"));
        order.setPincode(rs.getString("pincode"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            order.setCreatedAt(ts.toLocalDateTime());
        }
        return order;
    }
}
