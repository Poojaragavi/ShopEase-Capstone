package com.shopease.dto;

import com.shopease.model.OrderItem;
import com.shopease.util.CurrencyUtil;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object for an Order line item.
 */
public class OrderItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long sellerId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String formattedUnitPrice;
    private BigDecimal subtotal;
    private String formattedSubtotal;
    private boolean isReviewed;

    public OrderItemDTO() {
    }

    public static OrderItemDTO fromEntity(OrderItem item) {
        if (item == null) {
            return null;
        }
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrderId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setProductImageUrl(item.getProductImageUrl());
        dto.setSellerId(item.getSellerId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        BigDecimal sub = item.getSubtotal();
        dto.setSubtotal(sub);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.formattedUnitPrice = CurrencyUtil.formatINR(unitPrice);
    }

    public String getFormattedUnitPrice() {
        return formattedUnitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
        this.formattedSubtotal = CurrencyUtil.formatINR(subtotal);
    }

    public String getFormattedSubtotal() {
        return formattedSubtotal;
    }

    public boolean isReviewed() {
        return isReviewed;
    }

    public void setReviewed(boolean reviewed) {
        isReviewed = reviewed;
    }
}
