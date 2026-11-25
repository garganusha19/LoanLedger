import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        printBanner();

        boolean connected = testDatabaseConnection();

        showMainMenu(connected);
    }

    private static void printBanner() {
        System.out.println("============================================");
        System.out.println("             LOANLEDGER v1.0               ");
        System.out.println("         Loan Management System            ");
        System.out.println("============================================");
        System.out.println();
    }

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
            System.out.println();
            return false;
        }
    }

    private static void showMainMenu(boolean connected) {
        Scanner scanner = new Scanner(System.in);
        BorrowerService borrowerService = new BorrowerService();
        LoanService loanService = new LoanService();
        boolean running = true;

        while (running) {
            String status = connected ? "Connected" : "Not Connected";
            printMenu(status);

            String input = scanner.nextLine().trim();
            System.out.println();

            int choice = toNumber(input);

            switch (choice) {
                case 1:
                    BorrowerMenu borrowerMenu = new BorrowerMenu(borrowerService, scanner);
                    borrowerMenu.display();
                    break;

                case 2:
                    LoanMenu loanMenu = new LoanMenu(loanService, borrowerService, scanner);
                    loanMenu.display();
                    break;

                case 3:
                    showComingSoon("Payment Management");
                    break;

                case 4:
                    showComingSoon("Reports");
                    break;

                case 5:
                    connected = testDatabaseConnection();
                    break;

                case 6:
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

    private static void showComingSoon(String moduleName) {
        System.out.println("--- " + moduleName + " ---");
        System.out.println("This module is coming soon.");
        System.out.println();
    }

    private static int toNumber(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
