package com.shopease.controller;

import com.google.gson.JsonObject;
import com.shopease.dto.OrderDTO;
import com.shopease.model.OrderStatus;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.service.OrderService;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * REST API for Order checkout, tracking, and fulfillment status updates.
 * Endpoints:
 * - POST /api/v1/checkout
 * - GET  /api/v1/orders
 * - GET  /api/v1/orders/{id}
 * - PUT  /api/v1/orders/{id}/status
 */
@WebServlet(name = "OrderApiServlet", urlPatterns = {"/api/v1/orders", "/api/v1/orders/*", "/api/v1/checkout"})
public class OrderApiServlet extends BaseServlet {
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required.");
            return;
        }

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                if (currentUser.getRole() == Role.ADMIN) {
                    List<OrderDTO> orders = orderService.getAllOrders(currentUser);
                    sendSuccess(resp, orders);
                } else if (currentUser.getRole() == Role.SELLER) {
                    List<OrderDTO> orders = orderService.getSellerOrders(currentUser.getId());
                    sendSuccess(resp, orders);
                } else {
                    List<OrderDTO> orders = orderService.getBuyerOrders(currentUser.getId());
                    sendSuccess(resp, orders);
                }
            } else {
                Long orderId = extractIdFromPath(pathInfo);
                if (orderId == null) {
                    sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Invalid order ID.");
                    return;
                }
                OrderDTO order = orderService.getOrderById(currentUser, orderId);
                sendSuccess(resp, order);
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
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "Checkout details required.");
                return;
            }

            String customerName = body.has("customerName") ? body.get("customerName").getAsString() : currentUser.getName();
            String phone = body.has("phone") ? body.get("phone").getAsString() : null;
            String address = body.has("address") ? body.get("address").getAsString() : null;
            String city = body.has("city") ? body.get("city").getAsString() : null;
            String pincode = body.has("pincode") ? body.get("pincode").getAsString() : null;

            OrderDTO order = orderService.checkout(currentUser, customerName, phone, address, city, pincode);
            sendCreated(resp, order);
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

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.contains("/status")) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ENDPOINT", "Endpoint must be /api/v1/orders/{id}/status");
            return;
        }

        try {
            String[] parts = pathInfo.split("/");
            if (parts.length < 2) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Order ID required.");
                return;
            }
            Long orderId = Long.parseLong(parts[1]);

            JsonObject body = readJsonBody(req, JsonObject.class);
            if (body == null || !body.has("status")) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "'status' field is required.");
                return;
            }

            OrderStatus status = OrderStatus.fromString(body.get("status").getAsString());
            if (status == null) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_STATUS", "Invalid order status value.");
                return;
            }

            orderService.updateOrderStatus(currentUser, orderId, status);
            sendSuccess(resp, "Order status updated to " + status);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    private Long extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.length() <= 1) {
            return null;
        }
        String clean = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (clean.contains("/")) {
            clean = clean.substring(0, clean.indexOf("/"));
        }
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
