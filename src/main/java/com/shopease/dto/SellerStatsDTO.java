package com.shopease.dto;

import com.shopease.util.CurrencyUtil;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object for Seller Dashboard Metrics.
 */
public class SellerStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int totalProducts;
    private int lowStockProducts;
    private int outOfStockProducts;
    private int totalOrders;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private String formattedTotalRevenue = "₹0.00";

    public SellerStatsDTO() {
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(int lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public int getOutOfStockProducts() {
        return outOfStockProducts;
    }

    public void setOutOfStockProducts(int outOfStockProducts) {
        this.outOfStockProducts = outOfStockProducts;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
        this.formattedTotalRevenue = CurrencyUtil.formatINR(totalRevenue);
    }

    public String getFormattedTotalRevenue() {
        return formattedTotalRevenue;
    }
}
