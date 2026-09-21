package com.shopease.filter;

import com.shopease.dto.ApiResponse;
import com.shopease.model.Role;
import com.shopease.model.User;
import com.shopease.util.JsonUtil;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Role-Based Access Control Filter.
 * Protects buyer, seller, and admin routes with strict server-side authorization.
 * Returns HTTP 401 for unauthenticated API calls, redirects for unauthenticated web pages,
 * and HTTP 403 when authenticated but lacking permissions.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/*")
public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    public static final String SESSION_USER_KEY = "currentUser";

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        // Static resources and public routes
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute(SESSION_USER_KEY) : null;
        boolean isApi = path.startsWith("/api/");

        // Check if route requires authentication
        if (requiresAuthentication(path)) {
            if (currentUser == null) {
                if (isApi) {
                    sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required to access this resource.");
                } else {
                    resp.sendRedirect(contextPath + "/login?redirect=" + path);
                }
                return;
            }

            // Check Seller route authorization
            if (isSellerPath(path)) {
                if (currentUser.getRole() != Role.SELLER && currentUser.getRole() != Role.ADMIN) {
                    logger.warn("Authorization failure: User #{} (role={}) attempted to access seller route {}",
                            currentUser.getId(), currentUser.getRole(), path);
                    if (isApi) {
                        sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Access denied: Seller role required.");
                    } else {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Seller role required.");
                    }
                    return;
                }
            }

            // Check Admin route authorization
            if (isAdminPath(path)) {
                if (currentUser.getRole() != Role.ADMIN) {
                    logger.warn("Authorization failure: User #{} (role={}) attempted to access admin route {}",
                            currentUser.getId(), currentUser.getRole(), path);
                    if (isApi) {
                        sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Access denied: Administrator privileges required.");
                    } else {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required.");
                    }
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/static/") ||
               path.equals("/") ||
               path.equals("/index.jsp") ||
               path.equals("/home") ||
               path.equals("/products") ||
               path.equals("/product") ||
               path.equals("/login") ||
               path.equals("/register") ||
               path.equals("/logout") ||
               path.equals("/api/v1/health") ||
               path.equals("/api/v1/auth/login") ||
               path.equals("/api/v1/auth/register") ||
               path.equals("/api/v1/products") ||
               path.startsWith("/api/v1/products/") ||
               path.startsWith("/h2-console") ||
               path.equals("/api/v1/chat");
    }

    private boolean requiresAuthentication(String path) {
        return path.startsWith("/seller") ||
               path.startsWith("/admin") ||
               path.startsWith("/checkout") ||
               path.startsWith("/orders") ||
               path.startsWith("/order") ||
               path.startsWith("/profile") ||
               path.startsWith("/api/v1/seller") ||
               path.startsWith("/api/v1/admin") ||
               path.startsWith("/api/v1/checkout") ||
               path.startsWith("/api/v1/orders") ||
               path.startsWith("/api/v1/cart") ||
               path.startsWith("/api/v1/reviews") ||
               path.equals("/api/v1/auth/me");
    }

    private boolean isSellerPath(String path) {
        return path.startsWith("/seller") || path.startsWith("/api/v1/seller");
    }

    private boolean isAdminPath(String path) {
        return path.startsWith("/admin") || path.startsWith("/api/v1/admin");
    }

    private void sendJsonError(HttpServletResponse resp, int statusCode, String code, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> response = ApiResponse.error(code, message);
        resp.getWriter().write(JsonUtil.toJson(response));
    }

    @Override
    public void destroy() {
        // No destruction required
    }
}
