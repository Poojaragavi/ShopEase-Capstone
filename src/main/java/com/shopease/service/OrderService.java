package com.shopease.service;

import com.shopease.dao.CartDAO;
import com.shopease.dao.OrderDAO;
import com.shopease.dao.PaymentDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dto.OrderDTO;
import com.shopease.dto.SellerStatsDTO;
import com.shopease.exception.AuthorizationException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.exception.ValidationException;
import com.shopease.factory.DaoFactory;
import com.shopease.model.CartItem;
import com.shopease.model.Order;
import com.shopease.model.OrderItem;
import com.shopease.model.OrderStatus;
import com.shopease.model.Payment;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.payment.PaymentResult;
import com.shopease.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for checkout transactions, order fulfillment, status tracking, and seller revenue analytics.
 */
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    private final PaymentDAO paymentDAO;
    private final PaymentService paymentService;

    public OrderService() {
        this(DaoFactory.getOrderDAO(), DaoFactory.getCartDAO(), DaoFactory.getProductDAO(),
                DaoFactory.getPaymentDAO(), new PaymentService());
    }

    public OrderService(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO,
                        PaymentDAO paymentDAO, PaymentService paymentService) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.paymentDAO = paymentDAO;
        this.paymentService = paymentService;
    }

    /**
     * Executes server-side checkout:
     * 1. Loads active cart items from database
     * 2. Re-validates product stock & prices server-side
     * 3. Calculates total amount server-side
     * 4. Deducts stock atomically
     * 5. Creates order and order items
     * 6. Executes mock payment
     * 7. Clears user's active cart
     */
    public OrderDTO checkout(User buyer, String customerName, String phone, String address, String city, String pincode) {
        if (buyer == null) {
            throw new AuthorizationException("User must be logged in to checkout.");
        }

        ValidationUtil.requireNonBlank(customerName, "Full Name");
        ValidationUtil.validatePhone(phone);
        ValidationUtil.requireNonBlank(address, "Address");
        ValidationUtil.requireNonBlank(city, "City");
        ValidationUtil.validatePincode(pincode);

        // 1. Fetch active cart items from database
        List<CartItem> cartItems = cartDAO.findByUserId(buyer.getId(), false);
        if (cartItems.isEmpty()) {
            throw new ValidationException("Cannot checkout with an empty cart.");
        }

        // 2. Validate stock & calculate price server-side
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productDAO.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product in cart no longer exists."));

            if (product.getStockQty() < cartItem.getQuantity()) {
                throw new ValidationException("Insufficient stock for '" + product.getName() + "'. Available: " + product.getStockQty() + ", in cart: " + cartItem.getQuantity());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            calculatedTotal = calculatedTotal.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImageUrl(product.getImageUrl());
            orderItem.setSellerId(product.getSellerId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItems.add(orderItem);
        }

        // 3. Deduct stock for all items
        for (OrderItem item : orderItems) {
            boolean stockUpdated = productDAO.decrementStock(item.getProductId(), item.getQuantity());
            if (!stockUpdated) {
                throw new ValidationException("Stock became unavailable for product ID: " + item.getProductId() + " during checkout. Please try again.");
            }
        }

        // 4. Create Order entity
        Order order = new Order();
        order.setBuyerId(buyer.getId());
        order.setStatus(OrderStatus.CONFIRMED); // Mock payment automatically confirms
        order.setTotalAmount(calculatedTotal);
        order.setCustomerName(customerName.trim());
        order.setPhone(phone.trim());
        order.setAddress(address.trim());
        order.setCity(city.trim());
        order.setPincode(pincode.trim());
        order.setCreatedAt(LocalDateTime.now());

        Order createdOrder = orderDAO.createOrder(order, orderItems);
        logger.info("Created Order #{} for Buyer #{} total=₹{}", createdOrder.getId(), buyer.getId(), calculatedTotal);

        // 5. Process Mock Payment
        PaymentResult payResult = paymentService.executePayment(createdOrder.getId(), calculatedTotal, buyer.getEmail());

        // 6. Clear user's active cart
        cartDAO.clearActiveCart(buyer.getId());

        OrderDTO orderDTO = mapToOrderDTO(createdOrder);
        orderDTO.setTransactionId(payResult.getTransactionId());
        orderDTO.setPaymentMethod(payResult.getPaymentMethod());
        return orderDTO;
    }

    public OrderDTO getOrderById(User user, Long orderId) {
        if (user == null) {
            throw new AuthorizationException("Authentication required to view order.");
        }
        if (orderId == null) {
            throw new ValidationException("Order ID is required.");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Authorization check: Buyer can only see own orders; Sellers can see orders containing their products; Admins can see all.
        if (user.getRole() == Role.BUYER && !order.getBuyerId().equals(user.getId())) {
            throw new AuthorizationException("You are not authorized to view this order.");
        }

        if (user.getRole() == Role.SELLER) {
            boolean containsSellerProduct = order.getItems().stream()
                    .anyMatch(item -> user.getId().equals(item.getSellerId()));
            if (!containsSellerProduct) {
                throw new AuthorizationException("You are not authorized to view orders outside your product catalog.");
            }
        }

        return mapToOrderDTO(order);
    }

    public List<OrderDTO> getBuyerOrders(Long buyerId) {
        if (buyerId == null) {
            return new ArrayList<>();
        }
        return orderDAO.findByBuyerId(buyerId).stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getSellerOrders(Long sellerId) {
        if (sellerId == null) {
            return new ArrayList<>();
        }
        return orderDAO.findBySellerId(sellerId).stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders(User admin) {
        if (admin == null || admin.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Only administrators can view all system orders.");
        }
        return orderDAO.findAll().stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    public void updateOrderStatus(User user, Long orderId, OrderStatus newStatus) {
        if (user == null || (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN)) {
            throw new AuthorizationException("Only sellers or administrators can update order status.");
        }
        if (orderId == null || newStatus == null) {
            throw new ValidationException("Order ID and new status are required.");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (user.getRole() == Role.SELLER) {
            boolean hasProduct = order.getItems().stream().anyMatch(i -> user.getId().equals(i.getSellerId()));
            if (!hasProduct) {
                throw new AuthorizationException("You cannot update an order that does not contain your products.");
            }
        }

        OrderStatus currentStatus = order.getStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new ValidationException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        orderDAO.updateStatus(orderId, newStatus);
        logger.info("Order #{} status transitioned: {} ➔ {} by User #{}", orderId, currentStatus, newStatus, user.getId());
    }

    public SellerStatsDTO getSellerDashboardStats(Long sellerId) {
        if (sellerId == null) {
            throw new ValidationException("Seller ID is required.");
        }
        SellerStatsDTO stats = new SellerStatsDTO();
        stats.setTotalProducts(productDAO.countBySellerId(sellerId));
        stats.setLowStockProducts(productDAO.countLowStockBySellerId(sellerId, 5));
        stats.setOutOfStockProducts(productDAO.countOutOfStockBySellerId(sellerId));
        stats.setTotalOrders(orderDAO.countOrdersBySellerId(sellerId));
        stats.setTotalRevenue(orderDAO.calculateSellerRevenue(sellerId));
        return stats;
    }

    public int countTotalOrders() {
        return orderDAO.countTotalOrders();
    }

    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO dto = OrderDTO.fromEntity(order);
        if (dto != null && order.getId() != null) {
            Optional<Payment> p = paymentDAO.findByOrderId(order.getId());
            p.ifPresent(payment -> {
                dto.setTransactionId(payment.getTransactionId());
                dto.setPaymentMethod(payment.getPaymentMethod());
            });
        }
        return dto;
    }
}
