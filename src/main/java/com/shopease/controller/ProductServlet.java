package com.shopease.controller;

import com.shopease.dto.ProductDTO;
import com.shopease.dto.ReviewDTO;
import com.shopease.service.ProductService;
import com.shopease.service.ReviewService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling product catalog browsing, search/filter, and individual product details.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/products", "/product"})
public class ProductServlet extends BaseServlet {
    private final ProductService productService = new ProductService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();

        if ("/product".equals(servletPath)) {
            // Individual Product Details Page
            Long productId = parseLongParam(req, "id");
            if (productId == null) {
                resp.sendRedirect(req.getContextPath() + "/products");
                return;
            }

            try {
                ProductDTO product = productService.getProductById(productId);
                List<ReviewDTO> reviews = reviewService.getReviewsByProduct(productId);
                List<ProductDTO> relatedProducts = productService.getProductsByCategory(product.getCategory()).stream()
                        .filter(p -> !p.getId().equals(productId))
                        .limit(4).toList();

                req.setAttribute("product", product);
                req.setAttribute("reviews", reviews);
                req.setAttribute("relatedProducts", relatedProducts);
                req.getRequestDispatcher("/WEB-INF/views/buyer/product-details.jsp").forward(req, resp);
            } catch (Exception e) {
                req.setAttribute("errorMessage", e.getMessage());
                req.getRequestDispatcher("/WEB-INF/views/error/404.jsp").forward(req, resp);
            }
        } else {
            // Catalog / Search / Filter Page
            String keyword = req.getParameter("q");
            String category = req.getParameter("category");
            String sortBy = req.getParameter("sort");

            List<ProductDTO> products = productService.searchProducts(keyword, category, sortBy);
            List<String> categories = productService.getAllCategories();

            req.setAttribute("products", products);
            req.setAttribute("categories", categories);
            req.setAttribute("selectedCategory", category != null ? category : "all");
            req.setAttribute("keyword", keyword != null ? keyword : "");
            req.setAttribute("sortBy", sortBy != null ? sortBy : "newest");
            req.setAttribute("totalCount", products.size());

            req.getRequestDispatcher("/WEB-INF/views/buyer/products.jsp").forward(req, resp);
        }
    }
}
