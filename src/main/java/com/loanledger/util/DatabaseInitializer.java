package com.loanledger.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseInitializer — Utility to create and initialize the LoanLedger database.
 *
 * Responsibilities:
 * 1. Create loanledger_db database if it does not exist.
 * 2. Execute schema.sql to create all required tables, constraints, and relationships.
 * 3. Ensure idempotent execution (safe to run multiple times without duplicating or corrupting data).
 */
public class DatabaseInitializer {

    private static final String SCHEMA_FILE = "schema.sql";

    /**
     * Initializes the LoanLedger database and creates all tables defined in schema.sql.
     *
     * @return true if initialization succeeded, false otherwise
     * @throws SQLException if database execution encounters an error
     */
    public static boolean initialize() throws SQLException {
        // Step 1: Ensure database exists by connecting to the MySQL server
        try (Connection serverConn = DBConnection.getServerConnection();
             Statement stmt = serverConn.createStatement()) {

            String dbName = DBConnection.getDatabaseName();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName +
                    " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        // Step 2: Connect to the specific database and execute schema statements
        List<String> sqlStatements = loadSqlStatements(SCHEMA_FILE);
        if (sqlStatements.isEmpty()) {
            throw new SQLException("No SQL statements found in " + SCHEMA_FILE);
        }

        try (Connection dbConn = DBConnection.getConnection();
             Statement stmt = dbConn.createStatement()) {

            for (String sql : sqlStatements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }

        return true;
    }

    /**
     * Reads a SQL script file from the classpath and parses individual SQL statements.
     */
    private static List<String> loadSqlStatements(String resourcePath) {
        List<String> statements = new ArrayList<>();

        try (InputStream in = DatabaseInitializer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return statements;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder currentStatement = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();

                    // Skip empty lines and comment lines
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("/*")) {
                        continue;
                    }

                    currentStatement.append(line).append("\n");

                    // If statement ends with semicolon, add it to list
                    if (trimmed.endsWith(";")) {
                        statements.add(currentStatement.toString());
                        currentStatement.setLength(0);
                    }
                }

                if (currentStatement.length() > 0 && !currentStatement.toString().trim().isEmpty()) {
                    statements.add(currentStatement.toString());
                }
            }

        } catch (IOException e) {
            // Return whatever statements were collected so far
        }

        return statements;
    }

    private DatabaseInitializer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }
}

