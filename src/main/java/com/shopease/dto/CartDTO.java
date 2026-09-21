package com.shopease.dto;

import com.shopease.util.CurrencyUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for a user's entire cart including active items and saved items.
 */
public class CartDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<CartItemDTO> items = new ArrayList<>();
    private List<CartItemDTO> savedForLaterItems = new ArrayList<>();
    private int totalItemCount;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String formattedTotalAmount = "₹0.00";

    public CartDTO() {
    }

    public void calculateTotals() {
        int count = 0;
        BigDecimal sum = BigDecimal.ZERO;
        for (CartItemDTO item : items) {
            count += item.getQuantity();
            if (item.getSubtotal() != null) {
                sum = sum.add(item.getSubtotal());
            }
        }
        this.totalItemCount = count;
        this.totalAmount = sum;
        this.formattedTotalAmount = CurrencyUtil.formatINR(sum);
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items != null ? items : new ArrayList<>();
        calculateTotals();
    }

    public List<CartItemDTO> getSavedForLaterItems() {
        return savedForLaterItems;
    }

    public void setSavedForLaterItems(List<CartItemDTO> savedForLaterItems) {
        this.savedForLaterItems = savedForLaterItems != null ? savedForLaterItems : new ArrayList<>();
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }
}
