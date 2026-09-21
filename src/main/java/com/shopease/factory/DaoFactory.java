package com.shopease.factory;

import com.shopease.dao.CartDAO;
import com.shopease.dao.OrderDAO;
import com.shopease.dao.PaymentDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dao.ReviewDAO;
import com.shopease.dao.UserDAO;
import com.shopease.dao.impl.JdbcCartDAO;
import com.shopease.dao.impl.JdbcOrderDAO;
import com.shopease.dao.impl.JdbcPaymentDAO;
import com.shopease.dao.impl.JdbcProductDAO;
import com.shopease.dao.impl.JdbcReviewDAO;
import com.shopease.dao.impl.JdbcUserDAO;

/**
 * Factory Pattern implementation for instantiating DAO components.
 * Decouples service implementations from concrete JDBC DAO classes.
 */
public final class DaoFactory {
    private static final UserDAO USER_DAO = new JdbcUserDAO();
    private static final ProductDAO PRODUCT_DAO = new JdbcProductDAO();
    private static final CartDAO CART_DAO = new JdbcCartDAO();
    private static final OrderDAO ORDER_DAO = new JdbcOrderDAO();
    private static final ReviewDAO REVIEW_DAO = new JdbcReviewDAO();
    private static final PaymentDAO PAYMENT_DAO = new JdbcPaymentDAO();

    private DaoFactory() {
        // Factory class
    }

    public static UserDAO getUserDAO() {
        return USER_DAO;
    }

    public static ProductDAO getProductDAO() {
        return PRODUCT_DAO;
    }

    public static CartDAO getCartDAO() {
        return CART_DAO;
    }

    public static OrderDAO getOrderDAO() {
        return ORDER_DAO;
    }

    public static ReviewDAO getReviewDAO() {
        return REVIEW_DAO;
    }

    public static PaymentDAO getPaymentDAO() {
        return PAYMENT_DAO;
    }
}
