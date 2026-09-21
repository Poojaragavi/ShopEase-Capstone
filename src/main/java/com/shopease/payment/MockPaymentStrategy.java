package com.shopease.payment;

import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock payment strategy implementing simulated UPI / Card payments for ShopEase.
 */
public class MockPaymentStrategy implements PaymentStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MockPaymentStrategy.class);

    @Override
    public PaymentResult processPayment(Long orderId, BigDecimal amount, String customerEmail) {
        logger.info("Executing mock payment for order #{}, amount: ₹{}, customer: {}", orderId, amount, customerEmail);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentResult(false, null, amount, getProviderName(), "Payment failed: Invalid amount");
        }

        // Generate realistic transaction ID (e.g. SE-TXN-XXXXXXXX)
        String txnId = "SE-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        logger.info("Mock payment approved. Transaction ID: {}", txnId);

        return new PaymentResult(true, txnId, amount, getProviderName(), "Mock payment processed successfully");
    }

    @Override
    public String getProviderName() {
        return "ShopEase Mock Gateway (UPI/Card)";
    }
}
