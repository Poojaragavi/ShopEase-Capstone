package com.shopease.payment;

import java.math.BigDecimal;

/**
 * Strategy Pattern interface for pluggable payment processing strategies.
 */
public interface PaymentStrategy {
    /**
     * Executes a payment transaction.
     *
     * @param orderId the order ID
     * @param amount the monetary amount in INR
     * @param customerEmail the customer email
     * @return PaymentResult containing status and transaction ID
     */
    PaymentResult processPayment(Long orderId, BigDecimal amount, String customerEmail);

    /**
     * Returns the name of the payment provider.
     */
    String getProviderName();
}
