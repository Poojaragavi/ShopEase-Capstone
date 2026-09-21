package com.shopease.exception;

/**
 * Thrown when a low-level JDBC or database error occurs (HTTP 500).
 */
public class DatabaseException extends AppException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause, 500, "DATABASE_ERROR");
    }

    public DatabaseException(String message) {
        super(message, 500, "DATABASE_ERROR");
    }
}
