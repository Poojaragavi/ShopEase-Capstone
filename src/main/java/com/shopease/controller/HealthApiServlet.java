package com.shopease.controller;

import com.shopease.dto.HealthDTO;
import com.shopease.listener.AppContextListener;
import com.shopease.util.DatabaseUtil;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Health check endpoint providing real-time system and database connectivity status.
 * URL: GET /api/v1/health
 */
@WebServlet(name = "HealthApiServlet", urlPatterns = "/api/v1/health")
public class HealthApiServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean dbHealthy = DatabaseUtil.checkHealth();
        String status = dbHealthy ? "UP" : "DEGRADED";
        String dbStatus = dbHealthy ? "UP" : "DOWN";

        HealthDTO health = new HealthDTO(status, dbStatus, "1.0.0", AppContextListener.getUptimeSeconds());

        if (dbHealthy) {
            sendSuccess(resp, health);
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, health);
        }
    }
}
