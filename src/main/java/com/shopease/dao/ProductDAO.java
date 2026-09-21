package com.shopease.dao;

import com.shopease.model.Product;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Product operations.
 */
public interface ProductDAO {
    Product create(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByCategory(String category);
    List<Product> search(String keyword, String category, String sortBy);
    List<String> findAllCategories();
    boolean update(Product product);
    boolean updateStock(Long productId, int newStock);
    boolean decrementStock(Long productId, int quantity);
    boolean delete(Long id);
    int countBySellerId(Long sellerId);
    int countLowStockBySellerId(Long sellerId, int threshold);
    int countOutOfStockBySellerId(Long sellerId);
}
