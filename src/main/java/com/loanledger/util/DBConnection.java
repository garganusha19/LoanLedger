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
 * Responsible for loading database configuration from application.properties
 * and providing a live JDBC Connection to the MySQL database.
 *
 * Architectural Rules:
 * - Direct JDBC with DriverManager and Connection.
 * - Configuration is read from application.properties, NOT hardcoded.
 * - Each call to getConnection() returns a new Connection instance.
 * - Service classes must use try-with-resources to close the connection properly.
 */
public class DBConnection {

    // Configuration property keys in application.properties
    private static final String PROP_DRIVER   = "db.driver";
    private static final String PROP_URL      = "db.url";
    private static final String PROP_USERNAME = "db.username";
    private static final String PROP_PASSWORD = "db.password";
    private static final String PROP_DB_NAME  = "db.name";

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final Properties properties = new Properties();
    private static String loadError = null;

    static {
        loadProperties();
    }

    /**
     * Loads application.properties from classpath and registers the JDBC driver.
     */
    private static void loadProperties() {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                loadError = "application.properties not found on classpath. " +
                            "Please copy src/main/resources/application.properties.template to " +
                            "application.properties and configure your MySQL credentials.";
                return;
            }

            properties.load(input);

            // Register JDBC driver
            String driverClass = properties.getProperty(PROP_DRIVER, DEFAULT_DRIVER);
            if (driverClass != null && !driverClass.isBlank()) {
                try {
                    Class.forName(driverClass);
                } catch (ClassNotFoundException e) {
                    loadError = "MySQL JDBC Driver not found on classpath: " + driverClass;
                }
            }

        } catch (IOException e) {
            loadError = "Failed to load application.properties: " + e.getMessage();
        }
    }

    /**
     * Reloads configuration properties from application.properties.
     * Useful if configuration is created or updated while the application is running.
     */
    public static synchronized void reloadProperties() {
        properties.clear();
        loadError = null;
        loadProperties();
    }

    /**
     * Opens and returns a new JDBC Connection using the credentials
     * configured in application.properties.
     *
     * @return a live Connection to the MySQL database
     * @throws SQLException if configuration is missing or connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (loadError != null) {
            throw new SQLException(loadError);
        }

        String url      = properties.getProperty(PROP_URL);
        String username = properties.getProperty(PROP_USERNAME);
        String password = properties.getProperty(PROP_PASSWORD);

        if (url == null || url.isBlank()) {
            throw new SQLException("Property '" + PROP_URL + "' is missing or empty in application.properties.");
        }

        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Opens a JDBC Connection to the MySQL server without selecting a specific database.
     * Used for database initialization tasks such as CREATE DATABASE.
     *
     * @return a live Connection to the MySQL server
     * @throws SQLException if connection cannot be established
     */
    public static Connection getServerConnection() throws SQLException {
        if (loadError != null) {
            throw new SQLException(loadError);
        }

        String url = properties.getProperty(PROP_URL);
        String username = properties.getProperty(PROP_USERNAME);
        String password = properties.getProperty(PROP_PASSWORD);

        if (url == null || url.isBlank()) {
            throw new SQLException("Property '" + PROP_URL + "' is missing or empty in application.properties.");
        }

        // Strip database name from URL if present to connect directly to the MySQL instance
        // Pattern: jdbc:mysql://host:port/dbname?params -> jdbc:mysql://host:port/?params
        String serverUrl = url;
        int schemeEnd = url.indexOf("://");
        if (schemeEnd != -1) {
            int pathStart = url.indexOf('/', schemeEnd + 3);
            if (pathStart != -1) {
                int queryStart = url.indexOf('?', pathStart);
                if (queryStart != -1) {
                    serverUrl = url.substring(0, pathStart + 1) + url.substring(queryStart);
                } else {
                    serverUrl = url.substring(0, pathStart + 1);
                }
            }
        }

        return DriverManager.getConnection(serverUrl, username, password);
    }

    /**
     * Returns the configured database name.
     *
     * @return database name string, or "loanledger_db" as default
     */
    public static String getDatabaseName() {
        return properties.getProperty(PROP_DB_NAME, "loanledger_db");
    }

    /**
     * Checks if properties are loaded and not using placeholder credentials.
     *
     * @return true if properties file is found and has a URL configured
     */
    public static boolean isConfigured() {
        return loadError == null && properties.getProperty(PROP_URL) != null;
    }

    /**
     * Private constructor — DBConnection is a utility class and cannot be instantiated.
     */
    private DBConnection() {
        throw new UnsupportedOperationException("DBConnection is a utility class and cannot be instantiated.");
    }
}
