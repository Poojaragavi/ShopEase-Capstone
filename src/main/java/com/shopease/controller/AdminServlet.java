package com.shopease.controller;

import com.shopease.dto.OrderDTO;
import com.shopease.dto.ProductDTO;
import com.shopease.dto.UserDTO;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.service.OrderService;
import com.shopease.service.ProductService;
import com.shopease.service.ReviewService;
import com.shopease.service.UserService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling Admin Console: system metrics, user registry, and catalog moderation.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {
        "/admin/dashboard",
        "/admin/users",
        "/admin/products",
        "/admin/orders",
        "/admin/product/delete",
        "/admin/review/delete"
})
public class AdminServlet extends BaseServlet {
    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator role required.");
            return;
        }

        String path = req.getServletPath();

        if ("/admin/dashboard".equals(path)) {
            List<UserDTO> users = userService.findAllUsers();
            List<ProductDTO> products = productService.getAllProducts();
            List<OrderDTO> orders = orderService.getAllOrders(currentUser);

            req.setAttribute("totalUsers", users.size());
            req.setAttribute("totalProducts", products.size());
            req.setAttribute("totalOrders", orders.size());
            req.setAttribute("recentUsers", users.stream().limit(5).toList());
            req.setAttribute("recentOrders", orders.stream().limit(5).toList());
            req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);

        } else if ("/admin/users".equals(path)) {
            List<UserDTO> users = userService.findAllUsers();
            req.setAttribute("users", users);
            req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);

        } else if ("/admin/products".equals(path)) {
            List<ProductDTO> products = productService.getAllProducts();
            req.setAttribute("products", products);
            req.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(req, resp);

        } else if ("/admin/orders".equals(path)) {
            List<OrderDTO> orders = orderService.getAllOrders(currentUser);
            req.setAttribute("orders", orders);
            req.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(req, resp);

        } else if ("/admin/product/delete".equals(path)) {
            Long productId = parseLongParam(req, "id");
            if (productId != null) {
                try {
                    productService.deleteProduct(currentUser, productId);
                    req.getSession().setAttribute("successMessage", "Product #" + productId + " moderated/removed successfully.");
                } catch (Exception e) {
                    req.getSession().setAttribute("errorMessage", e.getMessage());
                }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/products");

        } else if ("/admin/review/delete".equals(path)) {
            Long reviewId = parseLongParam(req, "id");
            if (reviewId != null) {
                try {
                    reviewService.deleteReview(currentUser, reviewId);
                    req.getSession().setAttribute("successMessage", "Review #" + reviewId + " deleted successfully.");
                } catch (Exception e) {
                    req.getSession().setAttribute("errorMessage", e.getMessage());
                }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        }
    }
}
