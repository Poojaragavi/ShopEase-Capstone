package com.shopease.controller;

import com.shopease.dto.CartDTO;
import com.shopease.dto.OrderDTO;
import com.shopease.model.User;
import com.shopease.service.CartService;
import com.shopease.service.OrderService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet managing the checkout flow, delivery address submission, and mock payment execution.
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = "/checkout")
public class CheckoutServlet extends BaseServlet {
    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        CartDTO cart = cartService.getCart(currentUser.getId());
        if (cart.getItems().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        req.setAttribute("cart", cart);
        req.setAttribute("user", currentUser);
        req.getRequestDispatcher("/WEB-INF/views/buyer/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        String name = req.getParameter("customerName");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String city = req.getParameter("city");
        String pincode = req.getParameter("pincode");

        try {
            OrderDTO order = orderService.checkout(currentUser, name, phone, address, city, pincode);
            req.getSession().setAttribute("lastOrder", order);
            resp.sendRedirect(req.getContextPath() + "/order/success?id=" + order.getId());
        } catch (Exception e) {
            CartDTO cart = cartService.getCart(currentUser.getId());
            req.setAttribute("cart", cart);
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("customerName", name);
            req.setAttribute("phone", phone);
            req.setAttribute("address", address);
            req.setAttribute("city", city);
            req.setAttribute("pincode", pincode);
            req.getRequestDispatcher("/WEB-INF/views/buyer/checkout.jsp").forward(req, resp);
        }
    }
}
