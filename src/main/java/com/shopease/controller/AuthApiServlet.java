package com.shopease.controller;

import com.google.gson.JsonObject;
import com.shopease.dto.UserDTO;
import com.shopease.filter.AuthFilter;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.service.UserService;
import com.shopease.util.ConfigUtil;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * REST API for user authentication and session management.
 * Endpoints:
 * - POST /api/v1/auth/login
 * - POST /api/v1/auth/register
 * - GET  /api/v1/auth/me
 * - POST /api/v1/auth/logout
 */
@WebServlet(name = "AuthApiServlet", urlPatterns = "/api/v1/auth/*")
public class AuthApiServlet extends BaseServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/me".equals(pathInfo)) {
            User user = getCurrentUser(req);
            if (user == null) {
                sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "No active session.");
                return;
            }
            sendSuccess(resp, UserDTO.fromEntity(user));
        } else {
            sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if ("/login".equals(pathInfo)) {
                JsonObject body = readJsonBody(req, JsonObject.class);
                if (body == null || !body.has("email") || !body.has("password")) {
                    sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "Email and password are required.");
                    return;
                }
                String email = body.get("email").getAsString();
                String password = body.get("password").getAsString();

                User user = userService.authenticate(email, password);

                // Session fixation protection
                HttpSession oldSession = req.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }
                HttpSession session = req.getSession(true);
                int timeoutSeconds = ConfigUtil.getInt("session.timeout.seconds", 1800);
                session.setMaxInactiveInterval(timeoutSeconds);
                session.setAttribute(AuthFilter.SESSION_USER_KEY, user);
                session.setAttribute("userDTO", UserDTO.fromEntity(user));

                sendSuccess(resp, UserDTO.fromEntity(user));

            } else if ("/register".equals(pathInfo)) {
                JsonObject body = readJsonBody(req, JsonObject.class);
                if (body == null || !body.has("name") || !body.has("email") || !body.has("password")) {
                    sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "Name, email, and password are required.");
                    return;
                }
                String name = body.get("name").getAsString();
                String email = body.get("email").getAsString();
                String password = body.get("password").getAsString();
                Role role = body.has("role") ? Role.fromString(body.get("role").getAsString()) : Role.BUYER;

                UserDTO userDTO = userService.register(name, email, password, role);

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

                sendCreated(resp, userDTO);

            } else if ("/logout".equals(pathInfo)) {
                HttpSession session = req.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                sendSuccess(resp, "Logged out successfully.");
            } else {
                sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
