package com.shopease.util;

import com.shopease.exception.ValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {

    @Test
    public void testPasswordHashing() {
        String raw = "mySecurePassword123";
        String hash = PasswordUtil.hashPassword(raw);
        assertNotNull(hash);
        assertNotEquals(raw, hash);
        assertTrue(PasswordUtil.checkPassword(raw, hash));
        assertFalse(PasswordUtil.checkPassword("wrongPassword", hash));
    }

    @Test
    public void testCurrencyFormatting() {
        assertEquals("₹499.00", CurrencyUtil.formatINR(new BigDecimal("499.00")));
        assertEquals("₹1,299.00", CurrencyUtil.formatINR(new BigDecimal("1299.00")));
        assertEquals("₹1,299", CurrencyUtil.formatINRCompact(new BigDecimal("1299.00")));
        assertEquals("₹0.00", CurrencyUtil.formatINR(null));
    }

    @Test
    public void testValidationUtils() {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("buyer@shopease.com"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("invalid-email"));

        assertDoesNotThrow(() -> ValidationUtil.validatePassword("123456"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword("123"));

        assertDoesNotThrow(() -> ValidationUtil.validateRating(5));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(6));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(0));
    }
}
