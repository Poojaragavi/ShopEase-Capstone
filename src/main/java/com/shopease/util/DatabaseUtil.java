package com.shopease.util;

import com.shopease.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton database management utility wrapping HikariCP connection pool,
 * database migrations, and health checks.
 */
public final class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static volatile HikariDataSource dataSource;

    private DatabaseUtil() {
        // Utility class
    }

    /**
     * Initializes the HikariCP DataSource using configuration settings.
     */
    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        String dbPath = ConfigUtil.get("db.path");
        if (dbPath == null) {
            dbPath = ConfigUtil.get("h2.db.path");
        }
        String defaultUrl = (dbPath != null && !dbPath.trim().isEmpty())
                ? "jdbc:h2:file:" + dbPath.trim() + ";DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"
                : "jdbc:h2:file:./data/shopease;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
        String jdbcUrl = ConfigUtil.get("db.url", defaultUrl);
        String user = ConfigUtil.get("db.user", "sa");
        String password = ConfigUtil.get("db.password", "");
        String driver = ConfigUtil.get("db.driver", "org.h2.Driver");

        // Ensure parent directory exists for file-based H2 databases
        if (jdbcUrl.startsWith("jdbc:h2:file:")) {
            try {
                String rawPath = jdbcUrl.substring("jdbc:h2:file:".length());
                if (rawPath.contains(";")) {
                    rawPath = rawPath.substring(0, rawPath.indexOf(';'));
                }
                java.io.File dbFile = new java.io.File(rawPath);
                java.io.File parentDir = dbFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (created) {
                        logger.info("Created database directory: {}", parentDir.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not pre-create database directory: {}", e.getMessage());
            }
        }

        int maxPoolSize = ConfigUtil.getInt("db.pool.max_size", 10);
        int minIdle = ConfigUtil.getInt("db.pool.min_idle", 2);
        long connTimeout = ConfigUtil.getLong("db.pool.connection_timeout_ms", 30000);

        logger.info("Initializing HikariCP DataSource for URL: {}", jdbcUrl);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connTimeout);
        config.setPoolName("ShopEaseHikariPool");

        // H2 optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        logger.info("HikariCP DataSource initialized successfully.");

        // Run migrations
        runMigrations();
    }

    public static synchronized void initializeForTest(String testJdbcUrl) {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(testJdbcUrl);
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setPoolName("ShopEaseTestPool");
        dataSource = new HikariDataSource(config);
        runMigrations();
    }

    /**
     * Obtains a connection from the HikariCP connection pool.
     *
     * @return active JDBC Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }

    /**
     * Executes pending SQL migrations safely.
     */
    public static void runMigrations() {
        String[] migrationFiles = {
            "db/migrations/V1__init_schema.sql",
            "db/migrations/V2__create_indexes.sql"
        };

        try (Connection conn = getConnection()) {
            for (int i = 0; i < migrationFiles.length; i++) {
                String scriptPath = migrationFiles[i];
                int rank = i + 1;
                String version = "V" + rank;
                String description = scriptPath.substring(scriptPath.lastIndexOf('/') + 1);

                if (!isMigrationApplied(conn, rank)) {
                    logger.info("Applying migration {}: {}", version, scriptPath);
                    executeSqlScript(conn, scriptPath);
                    recordMigration(conn, rank, version, description, scriptPath, true);
                    logger.info("Migration {} applied successfully.", version);
                } else {
                    logger.debug("Migration {} already applied.", version);
                }
            }
        } catch (SQLException e) {
            logger.error("Database migration failure: {}", e.getMessage(), e);
            throw new DatabaseException("Database migration failed", e);
        }
    }

    private static boolean isMigrationApplied(Connection conn, int rank) {
        // First check if schema_version table exists
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SCHEMA_VERSION'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    return false;
                }
            }
        } catch (SQLException e) {
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT success FROM schema_version WHERE installed_rank = ?")) {
            ps.setInt(1, rank);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("success");
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static void recordMigration(Connection conn, int rank, String version,
                                        String description, String script, boolean success) throws SQLException {
        String sql = "MERGE INTO schema_version (installed_rank, version, description, script, success) KEY(installed_rank) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rank);
            ps.setString(2, version);
            ps.setString(3, description);
            ps.setString(4, script);
            ps.setBoolean(5, success);
            ps.executeUpdate();
        }
    }

    private static void executeSqlScript(Connection conn, String resourcePath) {
        InputStream in = DatabaseUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            logger.warn("Migration resource not found: {}", resourcePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                    sb.setLength(0);
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to execute SQL migration script: " + resourcePath, e);
        }
    }

    /**
     * Health check to test live database connectivity.
     *
     * @return true if database connection and query succeed, false otherwise
     */
    public static boolean checkHealth() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            return false;
        }
    }

    /**
     * Closes the connection pool gracefully.
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Shutting down HikariCP DataSource...");
            dataSource.close();
            dataSource = null;
            logger.info("HikariCP DataSource shut down cleanly.");
        }
    }
}
