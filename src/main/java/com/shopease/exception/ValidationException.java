package com.shopease.exception;

/**
 * Thrown when user input fails validation (HTTP 400).
 */
public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR");
    }
}
