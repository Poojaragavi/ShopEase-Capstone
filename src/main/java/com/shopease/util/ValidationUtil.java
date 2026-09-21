package com.shopease.util;

import com.shopease.exception.ValidationException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Common validation utilities for inputs across services.
 */
public final class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{7,15}$");
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^[0-9]{4,10}$");

    private ValidationUtil() {
        // Utility class
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be empty.");
        }
    }

    public static void validateEmail(String email) {
        requireNonBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validatePassword(String password) {
        requireNonBlank(password, "Password");
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
    }

    public static void validatePhone(String phone) {
        requireNonBlank(phone, "Phone");
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("Invalid phone number format: " + phone);
        }
    }

    public static void validatePincode(String pincode) {
        requireNonBlank(pincode, "Pincode");
        if (!PINCODE_PATTERN.matcher(pincode.trim()).matches()) {
            throw new ValidationException("Invalid pincode format: " + pincode);
        }
    }

    public static void validatePrice(BigDecimal price, String fieldName) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName + " must be a non-negative amount.");
        }
    }

    public static void validateStock(int stock) {
        if (stock < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }
    }

    public static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero.");
        }
    }

    public static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5 stars.");
        }
    }
}
