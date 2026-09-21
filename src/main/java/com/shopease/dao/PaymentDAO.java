package com.shopease.dao;

import com.shopease.model.Payment;
import java.util.Optional;

/**
 * Data Access Object interface for Payments.
 */
public interface PaymentDAO {
    Payment create(Payment payment);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
}
