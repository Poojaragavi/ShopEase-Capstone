package com.shopease.payment;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Result object returned by PaymentStrategy implementations.
 */
public class PaymentResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String transactionId;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String message;

    public PaymentResult(boolean success, String transactionId, BigDecimal amount, String paymentMethod, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getMessage() {
        return message;
    }
}
