package com.shopease.controller;

import com.shopease.dto.CartDTO;
import com.shopease.model.User;
import com.shopease.service.CartService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet managing the user's shopping cart and save-for-later actions.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/cart")
public class CartServlet extends BaseServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/cart");
            return;
        }

        CartDTO cart = cartService.getCart(currentUser.getId());
        req.setAttribute("cart", cart);
        req.getRequestDispatcher("/WEB-INF/views/buyer/cart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/cart");
            return;
        }

        String action = req.getParameter("action");
        Long cartItemId = parseLongParam(req, "cartItemId");
        Long productId = parseLongParam(req, "productId");

        try {
            if ("add".equalsIgnoreCase(action) && productId != null) {
                int qty = parseIntParam(req, "quantity", 1);
                cartService.addToCart(currentUser.getId(), productId, qty);
            } else if ("update".equalsIgnoreCase(action) && cartItemId != null) {
                int qty = parseIntParam(req, "quantity", 1);
                cartService.updateQuantity(currentUser.getId(), cartItemId, qty);
            } else if ("remove".equalsIgnoreCase(action) && cartItemId != null) {
                cartService.removeFromCart(currentUser.getId(), cartItemId);
            } else if ("saveForLater".equalsIgnoreCase(action) && cartItemId != null) {
                cartService.saveForLater(currentUser.getId(), cartItemId);
            } else if ("moveToCart".equalsIgnoreCase(action) && cartItemId != null) {
                cartService.moveToCart(currentUser.getId(), cartItemId);
            }
        } catch (Exception e) {
            req.getSession().setAttribute("cartErrorMessage", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/cart");
    }
}
