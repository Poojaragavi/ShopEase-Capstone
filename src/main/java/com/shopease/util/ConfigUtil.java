package com.shopease.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for loading application configuration from app.properties and environment variables.
 * Environment variables take precedence over properties file values.
 */
public final class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private ConfigUtil() {
        // Private constructor for utility class
    }

    private static void loadProperties() {
        try (InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in != null) {
                properties.load(in);
                logger.info("Loaded app.properties configuration successfully.");
            } else {
                logger.warn("app.properties not found on classpath, using defaults.");
            }
        } catch (IOException e) {
            logger.error("Failed to load app.properties", e);
        }
    }

    public static String get(String key, String defaultValue) {
        String envKey = key.toUpperCase().replace('.', '_').replace('-', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return properties.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for config '{}': {}", key, val);
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid long value for config '{}': {}", key, val);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val.trim());
    }
}
