package com.shopease.controller;

import com.shopease.dto.ApiResponse;
import com.shopease.exception.AppException;
import com.shopease.filter.AuthFilter;
import com.shopease.model.User;
import com.shopease.util.JsonUtil;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base abstract servlet providing common helper methods for JSON responses,
 * error handling, session management, and parameter parsing.
 */
public abstract class BaseServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);

    protected void sendJsonResponse(HttpServletResponse resp, int statusCode, Object data) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json;charset=UTF-8");
        ApiResponse<Object> response = ApiResponse.success(data);
        resp.getWriter().write(JsonUtil.toJson(response));
    }

    protected void sendSuccess(HttpServletResponse resp, Object data) throws IOException {
        sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    protected void sendCreated(HttpServletResponse resp, Object data) throws IOException {
        sendJsonResponse(resp, HttpServletResponse.SC_CREATED, data);
    }

    protected void sendJsonError(HttpServletResponse resp, int statusCode, String errorCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json;charset=UTF-8");
        ApiResponse<Object> response = ApiResponse.error(errorCode, message);
        resp.getWriter().write(JsonUtil.toJson(response));
    }

    protected void handleException(HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof AppException) {
            AppException ae = (AppException) e;
            logger.warn("Application exception: [{}] {}", ae.getErrorCode(), ae.getMessage());
            sendJsonError(resp, ae.getStatusCode(), ae.getErrorCode(), ae.getMessage());
        } else {
            logger.error("Unhandled server exception", e);
            sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again.");
        }
    }

    protected <T> T readJsonBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return JsonUtil.fromJson(sb.toString(), clazz);
    }

    protected User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            return (User) session.getAttribute(AuthFilter.SESSION_USER_KEY);
        }
        return null;
    }

    protected Long parseLongParam(HttpServletRequest req, String paramName) {
        String val = req.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected int parseIntParam(HttpServletRequest req, String paramName, int defaultValue) {
        String val = req.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
