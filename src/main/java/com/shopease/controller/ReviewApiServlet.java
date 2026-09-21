package com.shopease.controller;

import com.google.gson.JsonObject;
import com.shopease.dto.ReviewDTO;
import com.shopease.model.User;
import com.shopease.service.ReviewService;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * REST API for verified Product Reviews.
 * Endpoints:
 * - POST   /api/v1/reviews
 * - GET    /api/v1/reviews/product/{productId}
 * - DELETE /api/v1/reviews/{id}
 */
@WebServlet(name = "ReviewApiServlet", urlPatterns = {"/api/v1/reviews", "/api/v1/reviews/*"})
public class ReviewApiServlet extends BaseServlet {
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/product/")) {
            try {
                Long productId = Long.parseLong(pathInfo.substring("/product/".length()));
                List<ReviewDTO> list = reviewService.getReviewsByProduct(productId);
                sendSuccess(resp, list);
            } catch (NumberFormatException e) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Invalid product ID.");
            }
        } else {
            sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required to submit review.");
            return;
        }

        try {
            JsonObject body = readJsonBody(req, JsonObject.class);
            if (body == null || !body.has("productId") || !body.has("orderId") || !body.has("rating") || !body.has("comment")) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "productId, orderId, rating (1-5), and comment are required.");
                return;
            }

            Long productId = body.get("productId").getAsLong();
            Long orderId = body.get("orderId").getAsLong();
            int rating = body.get("rating").getAsInt();
            String comment = body.get("comment").getAsString();

            ReviewDTO review = reviewService.addReview(currentUser, productId, orderId, rating, comment);
            sendCreated(resp, review);
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

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Review ID required.");
            return;
        }

        try {
            Long reviewId = Long.parseLong(pathInfo.substring(1));
            reviewService.deleteReview(currentUser, reviewId);
            sendSuccess(resp, "Review deleted successfully.");
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
