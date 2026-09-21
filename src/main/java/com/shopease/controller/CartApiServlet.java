package com.shopease.controller;

import com.google.gson.JsonObject;
import com.shopease.dto.CartDTO;
import com.shopease.dto.CartItemDTO;
import com.shopease.model.User;
import com.shopease.service.CartService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * REST API for Cart operations.
 * Endpoints:
 * - GET    /api/v1/cart
 * - POST   /api/v1/cart
 * - PUT    /api/v1/cart/{id}
 * - DELETE /api/v1/cart/{id}
 * - DELETE /api/v1/cart
 */
@WebServlet(name = "CartApiServlet", urlPatterns = {"/api/v1/cart", "/api/v1/cart/*"})
public class CartApiServlet extends BaseServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required.");
            return;
        }

        CartDTO cart = cartService.getCart(currentUser.getId());
        sendSuccess(resp, cart);
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
            if (body == null || !body.has("productId")) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "productId is required.");
                return;
            }

            Long productId = body.get("productId").getAsLong();
            int quantity = body.has("quantity") ? body.get("quantity").getAsInt() : 1;

            CartItemDTO item = cartService.addToCart(currentUser.getId(), productId, quantity);
            sendCreated(resp, item);
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

        Long cartItemId = extractIdFromPath(req.getPathInfo());
        if (cartItemId == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Cart item ID required.");
            return;
        }

        try {
            JsonObject body = readJsonBody(req, JsonObject.class);
            if (body == null) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "Body required.");
                return;
            }

            if (body.has("quantity")) {
                int quantity = body.get("quantity").getAsInt();
                CartItemDTO item = cartService.updateQuantity(currentUser.getId(), cartItemId, quantity);
                sendSuccess(resp, item);
            } else if (body.has("savedForLater")) {
                boolean saved = body.get("savedForLater").getAsBoolean();
                if (saved) {
                    cartService.saveForLater(currentUser.getId(), cartItemId);
                } else {
                    cartService.moveToCart(currentUser.getId(), cartItemId);
                }
                sendSuccess(resp, "Cart item status updated.");
            } else {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "Either 'quantity' or 'savedForLater' is required.");
            }
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

        Long cartItemId = extractIdFromPath(req.getPathInfo());
        try {
            if (cartItemId != null) {
                cartService.removeFromCart(currentUser.getId(), cartItemId);
                sendSuccess(resp, "Cart item removed.");
            } else {
                cartService.clearActiveCart(currentUser.getId());
                sendSuccess(resp, "Active cart cleared.");
            }
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
