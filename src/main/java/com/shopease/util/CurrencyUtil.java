package com.shopease.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility for Indian Rupee (₹ INR) currency formatting.
 */
public final class CurrencyUtil {
    private static final String INR_SYMBOL = "₹";

    private CurrencyUtil() {
        // Utility class
    }

    /**
     * Formats a BigDecimal amount as an INR string (e.g., ₹1,299.00 or ₹99.00).
     *
     * @param amount the monetary amount
     * @return formatted INR string with ₹ symbol
     */
    public static String formatINR(BigDecimal amount) {
        if (amount == null) {
            return INR_SYMBOL + "0.00";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return INR_SYMBOL + formatter.format(amount);
    }

    /**
     * Formats a double/decimal as integer-style INR if no fraction (e.g., ₹1,299).
     *
     * @param amount the monetary amount
     * @return formatted INR string
     */
    public static String formatINRCompact(BigDecimal amount) {
        if (amount == null) {
            return INR_SYMBOL + "0";
        }
        BigDecimal rounded = amount.setScale(2, RoundingMode.HALF_UP);
        if (rounded.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
            return INR_SYMBOL + formatter.format(rounded.toBigInteger());
        }
        return formatINR(rounded);
    }

    public static BigDecimal roundMoney(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
