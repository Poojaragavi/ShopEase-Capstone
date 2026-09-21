package com.shopease.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for secure password hashing and verification using bcrypt.
 */
public final class PasswordUtil {
    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() {
        // Utility class
    }

    /**
     * Hashes a plaintext password using bcrypt with a salt.
     *
     * @param plainTextPassword the plaintext password
     * @return the bcrypt hashed string
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies a plaintext password against a stored bcrypt hash.
     *
     * @param plainTextPassword the candidate plaintext password
     * @param hashedPassword the stored bcrypt hash
     * @return true if matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
