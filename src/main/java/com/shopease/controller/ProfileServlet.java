package com.shopease.controller;

import com.shopease.dto.UserDTO;
import com.shopease.model.User;
import com.shopease.service.UserService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for viewing and managing user profile.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
public class ProfileServlet extends BaseServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/profile");
            return;
        }

        UserDTO userDTO = userService.findById(currentUser.getId());
        req.setAttribute("user", userDTO);
        req.getRequestDispatcher("/WEB-INF/views/buyer/profile.jsp").forward(req, resp);
    }
}
