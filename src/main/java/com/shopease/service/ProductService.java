package com.shopease.service;

import com.shopease.dao.ProductDAO;
import com.shopease.dao.ReviewDAO;
import com.shopease.dto.ProductDTO;
import com.shopease.exception.AuthorizationException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.exception.ValidationException;
import com.shopease.factory.DaoFactory;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.ValidationUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing product catalog, inventory, categories, search, and seller authorization.
 */
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductDAO productDAO;
    private final ReviewDAO reviewDAO;

    public ProductService() {
        this(DaoFactory.getProductDAO(), DaoFactory.getReviewDAO());
    }

    public ProductService(ProductDAO productDAO, ReviewDAO reviewDAO) {
        this.productDAO = productDAO;
        this.reviewDAO = reviewDAO;
    }

    public ProductDTO createProduct(User seller, String name, String description, String category,
                                    BigDecimal price, BigDecimal originalPrice, int stockQty, String imageUrl) {
        if (seller == null || seller.getRole() != Role.SELLER) {
            throw new AuthorizationException("Only registered sellers can create products.");
        }

        validateProductFields(name, category, price, originalPrice, stockQty);

        Product product = new Product();
        product.setSellerId(seller.getId());
        product.setName(name.trim());
        product.setDescription(description != null ? description.trim() : "");
        product.setCategory(category.trim());
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setStockQty(stockQty);
        product.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl.trim() : "/static/img/placeholder.png");
        product.calculateDiscount();

        Product created = productDAO.create(product);
        logger.info("Seller #{} created product #{}: '{}'", seller.getId(), created.getId(), created.getName());
        return mapToDTOWithReviews(created);
    }

    public ProductDTO updateProduct(User seller, Long productId, String name, String description, String category,
                                    BigDecimal price, BigDecimal originalPrice, int stockQty, String imageUrl) {
        if (seller == null || seller.getRole() != Role.SELLER) {
            throw new AuthorizationException("Only sellers can update products.");
        }
        if (productId == null) {
            throw new ValidationException("Product ID is required.");
        }

        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // Enforce Seller Ownership
        if (!existing.getSellerId().equals(seller.getId())) {
            logger.warn("Security violation: Seller #{} attempted to edit product #{} owned by Seller #{}",
                    seller.getId(), productId, existing.getSellerId());
            throw new AuthorizationException("You are not authorized to modify another seller's product.");
        }

        validateProductFields(name, category, price, originalPrice, stockQty);

        existing.setName(name.trim());
        existing.setDescription(description != null ? description.trim() : "");
        existing.setCategory(category.trim());
        existing.setPrice(price);
        existing.setOriginalPrice(originalPrice);
        existing.setStockQty(stockQty);
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            existing.setImageUrl(imageUrl.trim());
        }
        existing.calculateDiscount();

        boolean updated = productDAO.update(existing);
        if (!updated) {
            throw new ValidationException("Failed to update product details.");
        }

        logger.info("Seller #{} updated product #{}", seller.getId(), productId);
        return mapToDTOWithReviews(existing);
    }

    public void deleteProduct(User user, Long productId) {
        if (user == null || (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN)) {
            throw new AuthorizationException("Unauthorized to delete product.");
        }
        if (productId == null) {
            throw new ValidationException("Product ID is required.");
        }

        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // If seller, check ownership. If admin, allowed.
        if (user.getRole() == Role.SELLER && !existing.getSellerId().equals(user.getId())) {
            logger.warn("Security violation: Seller #{} attempted to delete product #{} owned by Seller #{}",
                    user.getId(), productId, existing.getSellerId());
            throw new AuthorizationException("You cannot delete another seller's product.");
        }

        productDAO.delete(productId);
        logger.info("Product #{} deleted by User #{} (role: {})", productId, user.getId(), user.getRole());
    }

    public ProductDTO getProductById(Long productId) {
        if (productId == null) {
            throw new ValidationException("Product ID is required.");
        }
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        return mapToDTOWithReviews(product);
    }

    public Optional<Product> getProductEntityById(Long productId) {
        return productDAO.findById(productId);
    }

    public List<ProductDTO> getAllProducts() {
        return productDAO.findAll().stream()
                .map(this::mapToDTOWithReviews)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsBySeller(Long sellerId) {
        if (sellerId == null) {
            throw new ValidationException("Seller ID is required.");
        }
        return productDAO.findBySellerId(sellerId).stream()
                .map(this::mapToDTOWithReviews)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByCategory(String category) {
        return productDAO.findByCategory(category).stream()
                .map(this::mapToDTOWithReviews)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProducts(String keyword, String category, String sortBy) {
        return productDAO.search(keyword, category, sortBy).stream()
                .map(this::mapToDTOWithReviews)
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return productDAO.findAllCategories();
    }

    public boolean updateStock(User seller, Long productId, int newStock) {
        if (seller == null || seller.getRole() != Role.SELLER) {
            throw new AuthorizationException("Only sellers can update inventory stock.");
        }
        ValidationUtil.validateStock(newStock);
        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        if (!existing.getSellerId().equals(seller.getId())) {
            throw new AuthorizationException("You are not authorized to modify stock for another seller's product.");
        }
        return productDAO.updateStock(productId, newStock);
    }

    private void validateProductFields(String name, String category, BigDecimal price, BigDecimal originalPrice, int stockQty) {
        ValidationUtil.requireNonBlank(name, "Product Name");
        ValidationUtil.requireNonBlank(category, "Category");
        ValidationUtil.validatePrice(price, "Price");
        ValidationUtil.validateStock(stockQty);

        if (originalPrice != null) {
            ValidationUtil.validatePrice(originalPrice, "Original Price");
            if (price.compareTo(originalPrice) > 0) {
                throw new ValidationException("Selling price (₹" + price + ") cannot be higher than original price (₹" + originalPrice + ").");
            }
        }
    }

    private ProductDTO mapToDTOWithReviews(Product p) {
        ProductDTO dto = ProductDTO.fromEntity(p);
        if (dto != null && p.getId() != null) {
            dto.setAverageRating(reviewDAO.getAverageRatingForProduct(p.getId()));
            dto.setReviewCount(reviewDAO.getReviewCountForProduct(p.getId()));
        }
        return dto;
    }
}
