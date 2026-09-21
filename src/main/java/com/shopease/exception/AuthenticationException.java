package com.shopease.exception;

/**
 * Thrown when an unauthenticated request attempts to access a protected resource (HTTP 401).
 */
public class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super(message, 401, "UNAUTHENTICATED");
    }
}
