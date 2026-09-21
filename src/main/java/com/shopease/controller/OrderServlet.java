package com.shopease.controller;

import com.shopease.dao.ReviewDAO;
import com.shopease.dto.OrderDTO;
import com.shopease.factory.DaoFactory;
import com.shopease.model.OrderStatus;
import com.shopease.model.User;
import com.shopease.service.OrderService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet managing order confirmation, buyer order history, and detailed order tracking.
 */
@WebServlet(name = "OrderServlet", urlPatterns = {"/orders", "/order", "/order/success"})
public class OrderServlet extends BaseServlet {
    private final OrderService orderService = new OrderService();
    private final ReviewDAO reviewDAO = DaoFactory.getReviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + req.getServletPath());
            return;
        }

        String path = req.getServletPath();

        if ("/order/success".equals(path)) {
            Long orderId = parseLongParam(req, "id");
            OrderDTO order = (OrderDTO) req.getSession().getAttribute("lastOrder");
            if (order == null && orderId != null) {
                try {
                    order = orderService.getOrderById(currentUser, orderId);
                } catch (Exception ignored) {
                }
            }
            if (order == null) {
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }
            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/views/buyer/order-success.jsp").forward(req, resp);
        } else if ("/order".equals(path)) {
            Long orderId = parseLongParam(req, "id");
            if (orderId == null) {
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }
            try {
                OrderDTO order = orderService.getOrderById(currentUser, orderId);
                // Check if items have already been reviewed by this buyer
                if (order.getItems() != null) {
                    order.getItems().forEach(item -> {
                        boolean reviewed = reviewDAO.existsByBuyerAndProductAndOrder(currentUser.getId(), item.getProductId(), order.getId());
                        item.setReviewed(reviewed);
                    });
                }
                req.setAttribute("order", order);
                req.setAttribute("isDelivered", order.getStatus() == OrderStatus.DELIVERED);
                req.getRequestDispatcher("/WEB-INF/views/buyer/order-details.jsp").forward(req, resp);
            } catch (Exception e) {
                req.setAttribute("errorMessage", e.getMessage());
                req.getRequestDispatcher("/WEB-INF/views/error/404.jsp").forward(req, resp);
            }
        } else {
            // Orders list
            List<OrderDTO> orders = orderService.getBuyerOrders(currentUser.getId());
            req.setAttribute("orders", orders);
            req.getRequestDispatcher("/WEB-INF/views/buyer/orders.jsp").forward(req, resp);
        }
    }
}
