package com.loanledger.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection — JDBC Connection Utility
 *
 * Responsible for reading database configuration from application.properties
 * and providing a live JDBC Connection to any class that needs it.
 *
 * Usage in a Service class:
 *
 *   try (Connection conn = DBConnection.getConnection()) {
 *       // use connection here
 *   }
 *
 * The try-with-resources block automatically closes the connection when done.
 *
 * Design decisions:
 * - Configuration is read from application.properties, NOT hardcoded here.
 * - Each call to getConnection() returns a new Connection.
 *   For this project size, a simple per-operation connection is sufficient.
 *   A connection pool can be added later if performance requires it.
 */
public class DBConnection {

    // Property keys expected in application.properties
    private static final String PROP_URL      = "db.url";
    private static final String PROP_USERNAME = "db.username";
    private static final String PROP_PASSWORD = "db.password";

    // Loaded once when this class is first used
    private static final Properties properties = new Properties();

    /*
     * Static initialiser — runs once when DBConnection is first referenced.
     * Loads application.properties from the classpath (src/main/resources/).
     */
    static {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new RuntimeException(
                    "application.properties not found on classpath. " +
                    "Ensure it exists in src/main/resources/."
                );
            }
            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to load application.properties: " + e.getMessage(), e
            );
        }
    }

    /**
     * Opens and returns a new JDBC Connection using the credentials
     * configured in application.properties.
     *
     * @return a live Connection to the MySQL database
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        String url      = properties.getProperty(PROP_URL);
        String username = properties.getProperty(PROP_USERNAME);
        String password = properties.getProperty(PROP_PASSWORD);

        if (url == null || url.isBlank()) {
            throw new SQLException(
                "db.url is not set in application.properties."
            );
        }

        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Private constructor — this is a utility class with only static methods.
     * It should never be instantiated.
     */
    private DBConnection() {
        throw new UnsupportedOperationException(
            "DBConnection is a utility class and cannot be instantiated."
        );
    }
}
