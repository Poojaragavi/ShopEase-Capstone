package com.shopease.exception;

/**
 * Thrown when an authenticated user attempts to access a forbidden resource (HTTP 403).
 */
public class AuthorizationException extends AppException {
    public AuthorizationException(String message) {
        super(message, 403, "FORBIDDEN");
    }
}
