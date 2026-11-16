package com.loanledger;

import com.loanledger.service.BorrowerService;
import com.loanledger.ui.BorrowerMenu;
import com.loanledger.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Main.java — Application Entry Point
 *
 * This is the first class Java runs when the program starts.
 * It performs:
 *   1. Displays the startup banner.
 *   2. Tests the MySQL database connection.
 *   3. Presents the main menu and routes to respective modules.
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        // Test database connection at startup
        boolean connected = testDatabaseConnection();

        // Show the main menu
        showMainMenu(connected);
    }

    // -----------------------------------------------------------------
    // METHOD 1: printBanner
    // -----------------------------------------------------------------
    private static void printBanner() {
        System.out.println("============================================");
        System.out.println("             LOANLEDGER v1.0               ");
        System.out.println("         Loan Management System            ");
        System.out.println("============================================");
        System.out.println();
    }

    // -----------------------------------------------------------------
    // METHOD 2: testDatabaseConnection
    // -----------------------------------------------------------------
    public static boolean testDatabaseConnection() {
        System.out.println("Testing database connection...");

        try (Connection conn = DBConnection.getConnection()) {
            String dbName = conn.getCatalog();
            System.out.println("Database Connection: Connected");
            System.out.println("Connected to database: " + dbName);
            System.out.println();
            return true;

        } catch (SQLException e) {
            System.out.println("Database Connection: FAILED");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Please check src/main/resources/application.properties.");
            System.out.println();
            return false;
        }
    }

    // -----------------------------------------------------------------
    // METHOD 3: showMainMenu
    // -----------------------------------------------------------------
    private static void showMainMenu(boolean connected) {
        Scanner scanner = new Scanner(System.in);
        BorrowerService borrowerService = new BorrowerService();
        boolean running = true;

        while (running) {
            String status = connected ? "Connected" : "Not Connected";
            printMenu(status);

            String input = scanner.nextLine().trim();
            System.out.println();

            int choice = toNumber(input);

            switch (choice) {
                case 1:
                    // Phase 2: Borrower Management
                    BorrowerMenu borrowerMenu = new BorrowerMenu(borrowerService, scanner);
                    borrowerMenu.display();
                    break;

                case 2:
                    showComingSoon("Loan Management");
                    break;

                case 3:
                    showComingSoon("Payment Management");
                    break;

                case 4:
                    showComingSoon("Reports");
                    break;

                case 5:
                    // Retry DB Connection
                    connected = testDatabaseConnection();
                    break;

                case 6:
                    // Exit
                    System.out.println("Thank you for using LoanLedger. Goodbye!");
                    System.out.println();
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 6.");
                    System.out.println();
                    break;
            }
        }
    }

    // -----------------------------------------------------------------
    // METHOD 4: printMenu
    // -----------------------------------------------------------------
    private static void printMenu(String connectionStatus) {
        System.out.println("Database Connection: " + connectionStatus);
        System.out.println();
        System.out.println("--------------- MAIN MENU -----------------");
        System.out.println();
        System.out.println("  1. Borrower Management");
        System.out.println("  2. Loan Management");
        System.out.println("  3. Payment Management");
        System.out.println("  4. Reports");
        System.out.println("  5. Retry Database Connection");
        System.out.println("  6. Exit");
        System.out.println();
        System.out.print("Enter your choice: ");
    }

    // -----------------------------------------------------------------
    // METHOD 5: showComingSoon
    // -----------------------------------------------------------------
    private static void showComingSoon(String moduleName) {
        System.out.println("--- " + moduleName + " ---");
        System.out.println("This module is coming soon.");
        System.out.println();
    }

    // -----------------------------------------------------------------
    // METHOD 6: toNumber
    // -----------------------------------------------------------------
    private static int toNumber(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
