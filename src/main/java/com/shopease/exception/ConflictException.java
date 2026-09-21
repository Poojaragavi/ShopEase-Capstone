package com.shopease.exception;

/**
 * Thrown when a conflict occurs such as duplicate email (HTTP 409).
 */
public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(message, 409, "CONFLICT");
    }
}
