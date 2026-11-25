package com.loanledger.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

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

    public static synchronized void reloadProperties() {
        properties.clear();
        loadError = null;
        loadProperties();
    }

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

    public static String getDatabaseName() {
        return properties.getProperty(PROP_DB_NAME, "loanledger_db");
    }

    public static boolean isConfigured() {
        return loadError == null && properties.getProperty(PROP_URL) != null;
    }

    private DBConnection() {
        throw new UnsupportedOperationException("DBConnection is a utility class and cannot be instantiated.");
    }
}
