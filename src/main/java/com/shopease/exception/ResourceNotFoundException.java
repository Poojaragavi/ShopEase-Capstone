package com.shopease.exception;

/**
 * Thrown when a requested resource is not found (HTTP 404).
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(message, 404, "NOT_FOUND");
    }
}
