package com.shopease.dto;

import com.shopease.model.Product;
import com.shopease.util.CurrencyUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Products.
 * Uses Builder Pattern for clean instantiations.
 */
public class ProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sellerId;
    private String sellerName;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String formattedPrice;
    private BigDecimal originalPrice;
    private String formattedOriginalPrice;
    private BigDecimal discountPercentage;
    private Integer stockQty;
    private boolean inStock;
    private boolean lowStock;
    private String imageUrl;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductDTO() {
    }

    private ProductDTO(Builder builder) {
        this.id = builder.id;
        this.sellerId = builder.sellerId;
        this.sellerName = builder.sellerName;
        this.name = builder.name;
        this.description = builder.description;
        this.category = builder.category;
        this.price = builder.price;
        this.formattedPrice = builder.formattedPrice != null ? builder.formattedPrice : CurrencyUtil.formatINR(builder.price);
        this.originalPrice = builder.originalPrice;
        this.formattedOriginalPrice = builder.originalPrice != null ? CurrencyUtil.formatINR(builder.originalPrice) : null;
        this.discountPercentage = builder.discountPercentage;
        this.stockQty = builder.stockQty;
        this.inStock = builder.stockQty != null && builder.stockQty > 0;
        this.lowStock = builder.stockQty != null && builder.stockQty > 0 && builder.stockQty <= 5;
        this.imageUrl = builder.imageUrl;
        this.averageRating = builder.averageRating;
        this.reviewCount = builder.reviewCount;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static ProductDTO fromEntity(Product p) {
        if (p == null) {
            return null;
        }
        return new Builder()
                .id(p.getId())
                .sellerId(p.getSellerId())
                .sellerName(p.getSellerName())
                .name(p.getName())
                .description(p.getDescription())
                .category(p.getCategory())
                .price(p.getPrice())
                .originalPrice(p.getOriginalPrice())
                .discountPercentage(p.getDiscountPercentage())
                .stockQty(p.getStockQty())
                .imageUrl(p.getImageUrl())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.formattedPrice = CurrencyUtil.formatINR(price);
    }

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
        this.formattedOriginalPrice = originalPrice != null ? CurrencyUtil.formatINR(originalPrice) : null;
    }

    public String getFormattedOriginalPrice() {
        return formattedOriginalPrice;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
        this.inStock = stockQty != null && stockQty > 0;
        this.lowStock = stockQty != null && stockQty > 0 && stockQty <= 5;
    }

    public boolean isInStock() {
        return inStock;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private Long id;
        private Long sellerId;
        private String sellerName;
        private String name;
        private String description;
        private String category;
        private BigDecimal price;
        private String formattedPrice;
        private BigDecimal originalPrice;
        private BigDecimal discountPercentage;
        private Integer stockQty;
        private String imageUrl;
        private Double averageRating;
        private Integer reviewCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder sellerId(Long sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder sellerName(String sellerName) {
            this.sellerName = sellerName;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder formattedPrice(String formattedPrice) {
            this.formattedPrice = formattedPrice;
            return this;
        }

        public Builder originalPrice(BigDecimal originalPrice) {
            this.originalPrice = originalPrice;
            return this;
        }

        public Builder discountPercentage(BigDecimal discountPercentage) {
            this.discountPercentage = discountPercentage;
            return this;
        }

        public Builder stockQty(Integer stockQty) {
            this.stockQty = stockQty;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder averageRating(Double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public Builder reviewCount(Integer reviewCount) {
            this.reviewCount = reviewCount;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ProductDTO build() {
            return new ProductDTO(this);
        }
    }
}
