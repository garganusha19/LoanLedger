package com.loanledger;

/**
 * LoanLedger — Application Entry Point
 *
 * This is the first class the JVM executes when the program starts.
 * At this stage it simply prints a startup banner to confirm the project
 * compiles and runs correctly.
 *
 * Future: This class will initialise the database connection and launch
 * the main menu (MainMenu.java in the ui package).
 */
public class Main {

    public static void main(String[] args) {
        printBanner();
        System.out.println("Application started successfully.");
        System.out.println("Full menu system coming soon.");
    }

    /**
     * Prints the application startup banner to the console.
     */
    private static void printBanner() {
        System.out.println("============================================");
        System.out.println("         LOANLEDGER v1.0                    ");
        System.out.println("   Loan Management System                   ");
        System.out.println("============================================");
        System.out.println();
    }
}
