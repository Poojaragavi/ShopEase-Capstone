package com.shopease.listener;

import com.shopease.util.DatabaseSeeder;
import com.shopease.util.DatabaseUtil;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServletContextListener managing application startup lifecycle,
 * HikariCP DataSource initialization, database migrations, and persistent seeding.
 */
@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);
    private static final long START_TIME = System.currentTimeMillis();

    public static long getUptimeSeconds() {
        return (System.currentTimeMillis() - START_TIME) / 1000;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("=================================================");
        logger.info("  Starting ShopEase E-Commerce Application...   ");
        logger.info("=================================================");

        try {
            // 1. Initialize Database & Migrations
            DatabaseUtil.initialize();

            // 2. Seed initial demo data safely (if not already present)
            DatabaseSeeder.seedIfEmpty();

            logger.info("ShopEase application context initialized successfully.");
        } catch (Exception e) {
            logger.error("FATAL: Failed to initialize ShopEase application context", e);
            throw new RuntimeException("Application startup failure", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Stopping ShopEase E-Commerce Application...");
        DatabaseUtil.shutdown();
        logger.info("ShopEase application stopped cleanly.");
    }
}
