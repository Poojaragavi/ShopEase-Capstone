package com.shopease.controller;

import com.shopease.dto.UserDTO;
import com.shopease.exception.AppException;
import com.shopease.filter.AuthFilter;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.service.UserService;
import com.shopease.util.ConfigUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet managing user authentication, login, registration, and logout.
 * Implements session fixation protection and explicit 30-minute session timeout.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute(AuthFilter.SESSION_USER_KEY);
                if (user != null) {
                    logger.info("Logging out user ID={}, email={}", user.getId(), user.getEmail());
                }
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login?loggedOut=true");
            return;
        }

        // If already logged in, redirect to appropriate home
        User currentUser = getCurrentUser(req);
        if (currentUser != null) {
            redirectBasedOnRole(resp, req.getContextPath(), currentUser.getRole());
            return;
        }

        if ("/register".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/register".equals(path)) {
            handleRegister(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String redirect = req.getParameter("redirect");

        try {
            User user = userService.authenticate(email, password);

            // Session fixation protection: invalidate old session and create fresh session
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = req.getSession(true);
            int timeoutSeconds = ConfigUtil.getInt("session.timeout.seconds", 1800);
            session.setMaxInactiveInterval(timeoutSeconds);
            session.setAttribute(AuthFilter.SESSION_USER_KEY, user);
            session.setAttribute("userDTO", UserDTO.fromEntity(user));

            logger.info("Session established for user {} with timeout {}s", user.getEmail(), timeoutSeconds);

            if (redirect != null && !redirect.trim().isEmpty() && !redirect.contains("login") && !redirect.contains("register")) {
                resp.sendRedirect(req.getContextPath() + redirect);
            } else {
                redirectBasedOnRole(resp, req.getContextPath(), user.getRole());
            }
        } catch (AppException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String roleStr = req.getParameter("role");

        Role role = Role.fromString(roleStr);
        if (role == null) {
            role = Role.BUYER;
        }

        try {
            userService.register(name, email, password, role);
            // Automatically log in after registration
            User user = userService.authenticate(email, password);

            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = req.getSession(true);
            int timeoutSeconds = ConfigUtil.getInt("session.timeout.seconds", 1800);
            session.setMaxInactiveInterval(timeoutSeconds);
            session.setAttribute(AuthFilter.SESSION_USER_KEY, user);
            session.setAttribute("userDTO", UserDTO.fromEntity(user));

            redirectBasedOnRole(resp, req.getContextPath(), user.getRole());
        } catch (AppException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.setAttribute("role", role != null ? role.name() : "BUYER");
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        }
    }

    private void redirectBasedOnRole(HttpServletResponse resp, String contextPath, Role role) throws IOException {
        if (role == Role.SELLER) {
            resp.sendRedirect(contextPath + "/seller/dashboard");
        } else if (role == Role.ADMIN) {
            resp.sendRedirect(contextPath + "/admin/dashboard");
        } else {
            resp.sendRedirect(contextPath + "/home");
        }
    }
}
