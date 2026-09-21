package com.shopease.controller;

import com.shopease.dto.OrderDTO;
import com.shopease.dto.ProductDTO;
import com.shopease.dto.SellerStatsDTO;
import com.shopease.model.OrderStatus;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.service.OrderService;
import com.shopease.service.ProductService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling Seller Portal: dashboard analytics, product catalog management, and order fulfillment.
 */
@WebServlet(name = "SellerServlet", urlPatterns = {
        "/seller/dashboard",
        "/seller/products",
        "/seller/orders",
        "/seller/product/new",
        "/seller/product/edit",
        "/seller/product/delete",
        "/seller/order/update"
})
public class SellerServlet extends BaseServlet {
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null || (currentUser.getRole() != Role.SELLER && currentUser.getRole() != Role.ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Seller role required.");
            return;
        }

        String path = req.getServletPath();

        if ("/seller/dashboard".equals(path)) {
            SellerStatsDTO stats = orderService.getSellerDashboardStats(currentUser.getId());
            List<ProductDTO> products = productService.getProductsBySeller(currentUser.getId());
            List<OrderDTO> orders = orderService.getSellerOrders(currentUser.getId());

            req.setAttribute("stats", stats);
            req.setAttribute("products", products.stream().limit(5).toList());
            req.setAttribute("orders", orders.stream().limit(5).toList());
            req.getRequestDispatcher("/WEB-INF/views/seller/dashboard.jsp").forward(req, resp);

        } else if ("/seller/products".equals(path)) {
            List<ProductDTO> products = productService.getProductsBySeller(currentUser.getId());
            List<String> categories = productService.getAllCategories();
            req.setAttribute("products", products);
            req.setAttribute("categories", categories);
            req.getRequestDispatcher("/WEB-INF/views/seller/products.jsp").forward(req, resp);

        } else if ("/seller/orders".equals(path)) {
            List<OrderDTO> orders = orderService.getSellerOrders(currentUser.getId());
            req.setAttribute("orders", orders);
            req.getRequestDispatcher("/WEB-INF/views/seller/orders.jsp").forward(req, resp);

        } else if ("/seller/product/delete".equals(path)) {
            Long productId = parseLongParam(req, "id");
            if (productId != null) {
                try {
                    productService.deleteProduct(currentUser, productId);
                    req.getSession().setAttribute("successMessage", "Product removed successfully.");
                } catch (Exception e) {
                    req.getSession().setAttribute("errorMessage", e.getMessage());
                }
            }
            resp.sendRedirect(req.getContextPath() + "/seller/products");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null || (currentUser.getRole() != Role.SELLER && currentUser.getRole() != Role.ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Seller role required.");
            return;
        }

        String path = req.getServletPath();

        if ("/seller/product/new".equals(path)) {
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String category = req.getParameter("category");
            String priceStr = req.getParameter("price");
            String originalPriceStr = req.getParameter("originalPrice");
            int stock = parseIntParam(req, "stockQty", 0);
            String imageUrl = req.getParameter("imageUrl");

            try {
                BigDecimal price = new BigDecimal(priceStr);
                BigDecimal origPrice = (originalPriceStr != null && !originalPriceStr.trim().isEmpty())
                        ? new BigDecimal(originalPriceStr) : null;

                productService.createProduct(currentUser, name, description, category, price, origPrice, stock, imageUrl);
                req.getSession().setAttribute("successMessage", "Product created successfully!");
            } catch (Exception e) {
                req.getSession().setAttribute("errorMessage", e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/seller/products");

        } else if ("/seller/product/edit".equals(path)) {
            Long id = parseLongParam(req, "id");
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String category = req.getParameter("category");
            String priceStr = req.getParameter("price");
            String originalPriceStr = req.getParameter("originalPrice");
            int stock = parseIntParam(req, "stockQty", 0);
            String imageUrl = req.getParameter("imageUrl");

            try {
                BigDecimal price = new BigDecimal(priceStr);
                BigDecimal origPrice = (originalPriceStr != null && !originalPriceStr.trim().isEmpty())
                        ? new BigDecimal(originalPriceStr) : null;

                productService.updateProduct(currentUser, id, name, description, category, price, origPrice, stock, imageUrl);
                req.getSession().setAttribute("successMessage", "Product updated successfully!");
            } catch (Exception e) {
                req.getSession().setAttribute("errorMessage", e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/seller/products");

        } else if ("/seller/order/update".equals(path)) {
            Long orderId = parseLongParam(req, "orderId");
            String statusStr = req.getParameter("status");

            try {
                OrderStatus status = OrderStatus.fromString(statusStr);
                orderService.updateOrderStatus(currentUser, orderId, status);
                req.getSession().setAttribute("successMessage", "Order #" + orderId + " updated to " + status);
            } catch (Exception e) {
                req.getSession().setAttribute("errorMessage", e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/seller/orders");
        }
    }
}
