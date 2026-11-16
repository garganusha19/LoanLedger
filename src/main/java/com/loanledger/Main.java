package com.loanledger;

import com.loanledger.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/*
 * Main.java — Application Entry Point
 *
 * This is the first class Java runs when the program starts.
 * It does three things:
 *   1. Prints the startup banner.
 *   2. Tests the MySQL database connection.
 *   3. Shows the main menu and waits for user input.
 */
public class Main {

    public static void main(String[] args) {

        printBanner();

        // Test the database connection and remember whether it succeeded.
        boolean connected = testDatabaseConnection();

        // Show the main menu. Pass 'connected' so the menu can display
        // "Connected" or "Not Connected" on every screen.
        showMainMenu(connected);
    }

    // -----------------------------------------------------------------
    // METHOD 1: printBanner
    // Prints the application title at the top when the program starts.
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
    // Tries to open a connection to MySQL.
    // Returns true if successful, false if it fails.
    // -----------------------------------------------------------------
    private static boolean testDatabaseConnection() {

        System.out.println("Testing database connection...");

        // try-with-resources automatically closes the connection when
        // the block ends — we do not need to call conn.close() manually.
        try (Connection conn = DBConnection.getConnection()) {

            // getCatalog() returns the name of the connected database.
            String dbName = conn.getCatalog();

            System.out.println("Database Connection: Connected");
            System.out.println("Connected to database: " + dbName);
            System.out.println();

            return true;

        } catch (SQLException e) {
            // If connection fails, print the error but do NOT print the password.
            System.out.println("Database Connection: FAILED");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Please check your application.properties file.");
            System.out.println();

            return false;
        }
    }

    // -----------------------------------------------------------------
    // METHOD 3: showMainMenu
    // Displays the menu in a loop until the user chooses Exit (5).
    // -----------------------------------------------------------------
    private static void showMainMenu(boolean connected) {

        // Set the status text shown on every menu screen.
        String status;
        if (connected) {
            status = "Connected";
        } else {
            status = "Not Connected";
        }

        // Scanner reads what the user types in the console.
        // Note: We do NOT close the Scanner because closing it also
        // closes System.in permanently, which causes problems.
        // The JVM cleans it up automatically when the program ends.
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {

            // Print the menu on screen.
            printMenu(status);

            // Read the line the user typed and remove extra spaces.
            String input = scanner.nextLine().trim();

            System.out.println();

            // Convert the input to a number.
            // If the user typed letters, toNumber() returns -1.
            int choice = toNumber(input);

            switch (choice) {

                case 1:
                    showComingSoon("Borrower Management");
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
                    // Exit — set running to false so the while loop ends.
                    System.out.println("Thank you for using LoanLedger. Goodbye!");
                    System.out.println();
                    running = false;
                    break;

                default:
                    // Any other number or text lands here.
                    System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                    System.out.println();
                    break;
            }
        }
    }


    // -----------------------------------------------------------------
    // METHOD 4: printMenu
    // Prints the menu options to the console.
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
        System.out.println("  5. Exit");
        System.out.println();
        System.out.print("Enter your choice: ");
    }


    // -----------------------------------------------------------------
    // METHOD 5: showComingSoon
    // Displays a placeholder message for modules not yet built.
    // -----------------------------------------------------------------
    private static void showComingSoon(String moduleName) {
        System.out.println("--- " + moduleName + " ---");
        System.out.println("This module is coming soon.");
        System.out.println();
    }


    // -----------------------------------------------------------------
    // METHOD 6: toNumber
    // Converts the user's text input to an integer.
    // If the text is not a valid number, it returns -1.
    // This prevents the program from crashing on bad input.
    // -----------------------------------------------------------------
    private static int toNumber(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
