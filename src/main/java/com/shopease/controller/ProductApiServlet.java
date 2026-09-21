package com.shopease.controller;

import com.google.gson.JsonObject;
import com.shopease.dto.ProductDTO;
import com.shopease.model.User;
import com.shopease.service.ProductService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * REST API for Product catalog operations.
 * Endpoints:
 * - GET    /api/v1/products
 * - GET    /api/v1/products/categories
 * - GET    /api/v1/products/{id}
 * - POST   /api/v1/products
 * - PUT    /api/v1/products/{id}
 * - DELETE /api/v1/products/{id}
 */
@WebServlet(name = "ProductApiServlet", urlPatterns = {"/api/v1/products", "/api/v1/products/*"})
public class ProductApiServlet extends BaseServlet {
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                String keyword = req.getParameter("q");
                String category = req.getParameter("category");
                String sortBy = req.getParameter("sort");
                List<ProductDTO> list = productService.searchProducts(keyword, category, sortBy);
                sendSuccess(resp, list);

            } else if ("/categories".equalsIgnoreCase(pathInfo)) {
                sendSuccess(resp, productService.getAllCategories());

            } else {
                Long productId = extractIdFromPath(pathInfo);
                if (productId == null) {
                    sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Invalid product ID.");
                    return;
                }
                ProductDTO product = productService.getProductById(productId);
                sendSuccess(resp, product);
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required.");
            return;
        }

        try {
            JsonObject body = readJsonBody(req, JsonObject.class);
            if (body == null) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "Request body is required.");
                return;
            }

            String name = body.has("name") ? body.get("name").getAsString() : null;
            String description = body.has("description") ? body.get("description").getAsString() : null;
            String category = body.has("category") ? body.get("category").getAsString() : null;
            BigDecimal price = body.has("price") ? body.get("price").getAsBigDecimal() : null;
            BigDecimal origPrice = body.has("originalPrice") ? body.get("originalPrice").getAsBigDecimal() : null;
            int stock = body.has("stockQty") ? body.get("stockQty").getAsInt() : 0;
            String imageUrl = body.has("imageUrl") ? body.get("imageUrl").getAsString() : null;

            ProductDTO created = productService.createProduct(currentUser, name, description, category, price, origPrice, stock, imageUrl);
            sendCreated(resp, created);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required.");
            return;
        }

        Long productId = extractIdFromPath(req.getPathInfo());
        if (productId == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Product ID required.");
            return;
        }

        try {
            JsonObject body = readJsonBody(req, JsonObject.class);
            if (body == null) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "Request body is required.");
                return;
            }

            String name = body.has("name") ? body.get("name").getAsString() : null;
            String description = body.has("description") ? body.get("description").getAsString() : null;
            String category = body.has("category") ? body.get("category").getAsString() : null;
            BigDecimal price = body.has("price") ? body.get("price").getAsBigDecimal() : null;
            BigDecimal origPrice = body.has("originalPrice") ? body.get("originalPrice").getAsBigDecimal() : null;
            int stock = body.has("stockQty") ? body.get("stockQty").getAsInt() : 0;
            String imageUrl = body.has("imageUrl") ? body.get("imageUrl").getAsString() : null;

            ProductDTO updated = productService.updateProduct(currentUser, productId, name, description, category, price, origPrice, stock, imageUrl);
            sendSuccess(resp, updated);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required.");
            return;
        }

        Long productId = extractIdFromPath(req.getPathInfo());
        if (productId == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Product ID required.");
            return;
        }

        try {
            productService.deleteProduct(currentUser, productId);
            sendSuccess(resp, "Product deleted successfully.");
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    private Long extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.length() <= 1) {
            return null;
        }
        String clean = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
