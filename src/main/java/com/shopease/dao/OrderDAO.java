package com.shopease.dao;

import com.shopease.model.Order;
import com.shopease.model.OrderItem;
import com.shopease.model.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Order and OrderItem operations.
 */
public interface OrderDAO {
    Order createOrder(Order order, List<OrderItem> items);
    Optional<Order> findById(Long orderId);
    List<Order> findByBuyerId(Long buyerId);
    List<Order> findAll();
    List<Order> findBySellerId(Long sellerId);
    boolean updateStatus(Long orderId, OrderStatus status);
    List<OrderItem> findItemsByOrderId(Long orderId);
    boolean hasPurchasedProduct(Long buyerId, Long productId);
    boolean isOrderDelivered(Long orderId);
    int countOrdersBySellerId(Long sellerId);
    BigDecimal calculateSellerRevenue(Long sellerId);
    int countTotalOrders();
}
