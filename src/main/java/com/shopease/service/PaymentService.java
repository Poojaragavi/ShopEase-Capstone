package com.shopease.service;

import com.shopease.dao.PaymentDAO;
import com.shopease.factory.DaoFactory;
import com.shopease.model.Payment;
import com.shopease.payment.MockPaymentStrategy;
import com.shopease.payment.PaymentResult;
import com.shopease.payment.PaymentStrategy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service managing payment processing via pluggable PaymentStrategy and transaction persistence.
 */
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentDAO paymentDAO;
    private PaymentStrategy paymentStrategy;

    public PaymentService() {
        this(DaoFactory.getPaymentDAO(), new MockPaymentStrategy());
    }

    public PaymentService(PaymentDAO paymentDAO, PaymentStrategy paymentStrategy) {
        this.paymentDAO = paymentDAO;
        this.paymentStrategy = paymentStrategy != null ? paymentStrategy : new MockPaymentStrategy();
    }

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }

    public PaymentResult executePayment(Long orderId, BigDecimal amount, String customerEmail) {
        PaymentResult result = paymentStrategy.processPayment(orderId, amount, customerEmail);

        Payment payment = new Payment(
                null,
                orderId,
                result.getTransactionId(),
                result.getAmount(),
                result.getPaymentMethod(),
                result.isSuccess() ? "SUCCESS" : "FAILED",
                LocalDateTime.now()
        );

        paymentDAO.create(payment);
        logger.info("Recorded payment transaction {} for order #{}", result.getTransactionId(), orderId);
        return result;
    }

    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentDAO.findByOrderId(orderId);
    }
}
