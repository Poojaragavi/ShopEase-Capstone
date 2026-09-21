package com.shopease.dto;

import com.shopease.model.Order;
import com.shopease.model.OrderStatus;
import com.shopease.util.CurrencyUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for an Order.
 * Uses Builder Pattern for clean instantiations.
 */
public class OrderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long buyerId;
    private String buyerEmail;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String formattedTotalAmount;
    private String customerName;
    private String phone;
    private String address;
    private String city;
    private String pincode;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items = new ArrayList<>();
    private String transactionId;
    private String paymentMethod;

    public OrderDTO() {
    }

    private OrderDTO(Builder builder) {
        this.id = builder.id;
        this.buyerId = builder.buyerId;
        this.buyerEmail = builder.buyerEmail;
        this.status = builder.status;
        this.totalAmount = builder.totalAmount;
        this.formattedTotalAmount = builder.totalAmount != null ? CurrencyUtil.formatINR(builder.totalAmount) : "₹0.00";
        this.customerName = builder.customerName;
        this.phone = builder.phone;
        this.address = builder.address;
        this.city = builder.city;
        this.pincode = builder.pincode;
        this.createdAt = builder.createdAt;
        this.items = builder.items != null ? builder.items : new ArrayList<>();
        this.transactionId = builder.transactionId;
        this.paymentMethod = builder.paymentMethod;
    }

    public static OrderDTO fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        if (order.getItems() != null) {
            order.getItems().forEach(item -> itemDTOs.add(OrderItemDTO.fromEntity(item)));
        }
        return new Builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .buyerEmail(order.getBuyerEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .customerName(order.getCustomerName())
                .phone(order.getPhone())
                .address(order.getAddress())
                .city(order.getCity())
                .pincode(order.getPincode())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .build();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        this.formattedTotalAmount = CurrencyUtil.formatINR(totalAmount);
    }

    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public static class Builder {
        private Long id;
        private Long buyerId;
        private String buyerEmail;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private String customerName;
        private String phone;
        private String address;
        private String city;
        private String pincode;
        private LocalDateTime createdAt;
        private List<OrderItemDTO> items = new ArrayList<>();
        private String transactionId;
        private String paymentMethod;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder buyerId(Long buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public Builder buyerEmail(String buyerEmail) {
            this.buyerEmail = buyerEmail;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder pincode(String pincode) {
            this.pincode = pincode;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder items(List<OrderItemDTO> items) {
            this.items = items;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public OrderDTO build() {
            return new OrderDTO(this);
        }
    }
}
