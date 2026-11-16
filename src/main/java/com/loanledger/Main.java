package com.loanledger;

import com.loanledger.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * LoanLedger — Application Entry Point
 *
 * This class:
 * 1. Prints the startup banner.
 * 2. Tests the MySQL database connection using DBConnection.
 * 3. Displays the main menu in a loop until the user chooses Exit.
 *
 * Architecture note:
 * - Database connectivity lives in DBConnection (util package).
 * - Business logic will live in Service classes (service package).
 * - This class only handles startup and top-level menu routing.
 */
public class Main {

    public static void main(String[] args) {

        printBanner();

        // ── Step 1: Test the database connection ──────────────────────────
        boolean connected = testDatabaseConnection();

        // ── Step 2: Launch the main menu ──────────────────────────────────
        // We pass the connection status to the menu so it can display it.
        runMainMenu(connected);
    }

    // =========================================================================
    // DATABASE CONNECTION TEST
    // =========================================================================

    /**
     * Attempts to open a JDBC connection using DBConnection.getConnection().
     *
     * Uses try-with-resources so the Connection is closed automatically
     * after the test — no manual close() call is needed.
     *
     * @return true if the connection succeeded, false if it failed.
     */
    private static boolean testDatabaseConnection() {
        System.out.println("Testing database connection...");

        // try-with-resources: the Connection 'conn' is automatically closed
        // when this block ends, whether it succeeds or throws an exception.
        try (Connection conn = DBConnection.getConnection()) {

            // getCatalog() returns the name of the database we are connected to.
            // We print it to confirm we reached the right database.
            String databaseName = conn.getCatalog();

            System.out.println("Database Connection: Connected");
            System.out.println("Connected to database: " + databaseName);
            System.out.println();
            return true;

        } catch (SQLException e) {
            // Print a clear error message WITHOUT revealing the password.
            System.out.println("Database Connection: FAILED");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Check your application.properties settings.");
            System.out.println();
            // The application continues even if the DB is unreachable,
            // so the menu is still visible. Features requiring the DB
            // will handle failures individually when implemented.
            return false;
        }
    }

    // =========================================================================
    // MAIN MENU
    // =========================================================================

    /**
     * Displays the main menu in a loop.
     *
     * The loop runs until the user enters 5 (Exit).
     * Invalid inputs are handled without crashing.
     *
     * Scanner note:
     * We do NOT close the Scanner in a finally block or try-with-resources
     * because closing a Scanner that wraps System.in also closes System.in
     * permanently, which can break input in the same JVM session.
     * The Scanner is left open and the JVM cleans it up on exit — this is
     * the standard practice for console applications in Java.
     *
     * @param dbConnected whether the DB connection test passed
     */
    private static void runMainMenu(boolean dbConnected) {
        Scanner scanner = new Scanner(System.in);

        // connectionStatus is shown in the banner area of every menu display.
        String connectionStatus = dbConnected ? "Connected" : "Not Connected";

        boolean running = true;

        while (running) {

            printMenuHeader(connectionStatus);

            // Read the user's raw input, trim whitespace
            String input = scanner.nextLine().trim();

            System.out.println(); // blank line after input for readability

            // Parse the input to an integer safely
            int choice = parseChoice(input);

            switch (choice) {
                case 1 -> handleModuleStub("Borrower Management");
                case 2 -> handleModuleStub("Loan Management");
                case 3 -> handleModuleStub("Payment Management");
                case 4 -> handleModuleStub("Reports");
                case 5 -> {
                    running = false;
                    System.out.println("Thank you for using LoanLedger. Goodbye!");
                    System.out.println();
                    // System.exit(0) is called here — not in a service or model —
                    // because only the entry point should control JVM shutdown.
                    // This also cleanly terminates the MySQL Connector background thread.
                    System.exit(0);
                }
                default -> {
                    System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                    System.out.println();
                }
            }
        }
        // No scanner.close() here — see the Javadoc above for the reason.
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Prints the application startup banner.
     */
    private static void printBanner() {
        System.out.println("============================================");
        System.out.println("             LOANLEDGER v1.0               ");
        System.out.println("         Loan Management System            ");
        System.out.println("============================================");
        System.out.println();
    }

    /**
     * Prints the main menu with the current DB connection status.
     *
     * @param connectionStatus "Connected" or "Not Connected"
     */
    private static void printMenuHeader(String connectionStatus) {
        System.out.println("Database Connection: " + connectionStatus);
        System.out.println();
        System.out.println("--------------- MAIN MENU -----------------");
        System.out.println();
        System.out.println("  1. Borrower Management");
        System.out.println("  2. Loan Management");
        System.out.println("  3. Payment Management");
        System.out.println("  4. Reports");
        System.out.println("  5. Exit");
        System.out.println();
        System.out.print("Enter your choice: ");
    }

    /**
     * Displays a "coming soon" message for unimplemented modules.
     *
     * @param moduleName the name of the module chosen
     */
    private static void handleModuleStub(String moduleName) {
        System.out.println("--- " + moduleName + " ---");
        System.out.println("This module is coming soon.");
        System.out.println();
    }

    /**
     * Safely converts a String to an integer.
     * Returns -1 if the input is not a valid number, so the
     * switch statement's default case handles it as invalid.
     *
     * @param input raw user input string
     * @return parsed integer, or -1 on failure
     */
    private static int parseChoice(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            // Input was not a number — return -1 to trigger the default case
            return -1;
        }
    }
}
