package com.shopease.dto;

import com.shopease.model.CartItem;
import com.shopease.util.CurrencyUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for a single cart item.
 */
public class CartItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private String category;
    private BigDecimal unitPrice;
    private String formattedUnitPrice;
    private Integer quantity;
    private Integer availableStock;
    private BigDecimal subtotal;
    private String formattedSubtotal;
    private Boolean savedForLater;
    private LocalDateTime createdAt;

    public CartItemDTO() {
    }

    public static CartItemDTO fromEntity(CartItem item) {
        if (item == null) {
            return null;
        }
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setUserId(item.getUserId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setSavedForLater(item.getSavedForLater());
        dto.setCreatedAt(item.getCreatedAt());

        if (item.getProduct() != null) {
            dto.setProductName(item.getProduct().getName());
            dto.setProductImageUrl(item.getProduct().getImageUrl());
            dto.setCategory(item.getProduct().getCategory());
            dto.setUnitPrice(item.getProduct().getPrice());
            dto.setAvailableStock(item.getProduct().getStockQty());

            BigDecimal sub = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            dto.setSubtotal(sub);
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
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

    public Boolean getSavedForLater() {
        return savedForLater;
    }

    public void setSavedForLater(Boolean savedForLater) {
        this.savedForLater = savedForLater;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
