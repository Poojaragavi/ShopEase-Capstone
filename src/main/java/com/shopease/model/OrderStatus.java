package com.shopease.model;

/**
 * Order fulfillment statuses.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus fromString(String statusStr) {
        if (statusStr == null) {
            return null;
        }
        try {
            return OrderStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates whether a transition from current status to target status is permitted.
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (this == target) {
            return true;
        }
        switch (this) {
            case PENDING:
                return target == CONFIRMED || target == CANCELLED;
            case CONFIRMED:
                return target == SHIPPED || target == CANCELLED;
            case SHIPPED:
                return target == DELIVERED;
            case DELIVERED:
            case CANCELLED:
            default:
                return false;
        }
    }
}
