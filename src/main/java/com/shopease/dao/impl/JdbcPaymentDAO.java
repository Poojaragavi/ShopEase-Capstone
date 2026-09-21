package com.shopease.dao.impl;

import com.shopease.dao.PaymentDAO;
import com.shopease.exception.DatabaseException;
import com.shopease.model.Payment;
import com.shopease.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC implementation of PaymentDAO.
 */
public class JdbcPaymentDAO implements PaymentDAO {
    private static final Logger logger = LoggerFactory.getLogger(JdbcPaymentDAO.class);

    @Override
    public Payment create(Payment payment) {
        String sql = "INSERT INTO payments (order_id, transaction_id, amount, payment_method, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, payment.getOrderId());
            ps.setString(2, payment.getTransactionId());
            ps.setBigDecimal(3, payment.getAmount());
            ps.setString(4, payment.getPaymentMethod());
            ps.setString(5, payment.getStatus() != null ? payment.getStatus() : "SUCCESS");
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setTimestamp(6, now);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Creating payment failed.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    payment.setId(rs.getLong(1));
                    payment.setCreatedAt(now.toLocalDateTime());
                    return payment;
                } else {
                    throw new DatabaseException("Creating payment failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating payment record", e);
            throw new DatabaseException("Error creating payment record", e);
        }
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, order_id, transaction_id, amount, payment_method, status, created_at FROM payments WHERE order_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPayment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payment by order ID: {}", orderId, e);
            throw new DatabaseException("Error finding payment by order ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = "SELECT id, order_id, transaction_id, amount, payment_method, status, created_at FROM payments WHERE transaction_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPayment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payment by transaction ID: {}", transactionId, e);
            throw new DatabaseException("Error finding payment by transaction ID", e);
        }
        return Optional.empty();
    }

    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setOrderId(rs.getLong("order_id"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
