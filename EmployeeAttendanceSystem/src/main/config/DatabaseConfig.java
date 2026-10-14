package main.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseConfig – manages JDBC connection to MySQL.
 *
 * Reads connection parameters from db.properties on the classpath,
 * falling back to sensible defaults so the app can start without
 * a config file present.
 */
public class DatabaseConfig {

    private static final Logger LOG = Logger.getLogger(DatabaseConfig.class.getName());

    // ── defaults (overridden by db.properties) ──────────────────────────────
    private static String HOST     = "localhost";
    private static String PORT     = "3306";
    private static String DATABASE = "attendance_db";
    private static String USERNAME = "root";
    private static String PASSWORD = "";

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
                HOST     = props.getProperty("db.host",     HOST);
                PORT     = props.getProperty("db.port",     PORT);
                DATABASE = props.getProperty("db.name",     DATABASE);
                USERNAME = props.getProperty("db.username", USERNAME);
                PASSWORD = props.getProperty("db.password", PASSWORD);
            }
        } catch (IOException e) {
            LOG.warning("db.properties not found – using defaults.");
        }

        // Register JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOG.severe("MySQL JDBC driver not found on classpath: " + e.getMessage());
        }
    }

    /** Returns a fresh Connection from DriverManager. */
    public static Connection getConnection() throws SQLException {
        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=Asia/Kolkata" +
            "&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
            HOST, PORT, DATABASE);
        return DriverManager.getConnection(url, USERNAME, PASSWORD);
    }

    /** Safe close for a Connection (null-safe). */
    public static void close(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) {
                LOG.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }

    // ── Getters (for display in Settings panel) ─────────────────────────────
    public static String getHost()     { return HOST;     }
    public static String getPort()     { return PORT;     }
    public static String getDatabase() { return DATABASE; }
    public static String getUsername() { return USERNAME; }

    /** Test that we can reach the database. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
