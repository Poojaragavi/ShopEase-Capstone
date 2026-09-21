package com.shopease.dto;

import java.io.Serializable;

/**
 * Health check status DTO for /api/v1/health.
 */
public class HealthDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;
    private String db;
    private String version;
    private long uptimeSeconds;

    public HealthDTO() {
    }

    public HealthDTO(String status, String db, String version, long uptimeSeconds) {
        this.status = status;
        this.db = db;
        this.version = version;
        this.uptimeSeconds = uptimeSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }
}
