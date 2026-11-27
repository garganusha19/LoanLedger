import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        printBanner();

        boolean connected = testDatabaseConnection();

        Scanner scanner = new Scanner(System.in);
        AuthService authService = new AuthService();
        LoginMenu loginMenu = new LoginMenu(authService, scanner);

        boolean appRunning = true;

        while (appRunning) {
            Lender currentLender = loginMenu.display();

            if (currentLender == null) {
                System.out.println("Thank you for using LoanLedger. Goodbye!");
                System.out.println();
                break;
            }

            boolean wantLoginAgain = showMainMenu(scanner, connected, currentLender);

            // Clear current lender session upon logout or exit
            currentLender = null;

            if (!wantLoginAgain) {
                appRunning = false;
            }
        }
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


    private static boolean showMainMenu(Scanner scanner, boolean connected, Lender currentLender) {
        int lenderId = currentLender.getId();
        BorrowerService borrowerService = new BorrowerService(lenderId);
        LoanService loanService = new LoanService(lenderId);
        InstalmentService instalmentService = new InstalmentService(lenderId);
        boolean running = true;

        while (running) {
            String status = connected ? "Connected" : "Not Connected";
            printMenu(status, currentLender.getUsername());

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
                    InstalmentMenu instalmentMenu = new InstalmentMenu(instalmentService, loanService, scanner);
                    instalmentMenu.display();
                    break;

                case 4:
                    showComingSoon("Payment Management");
                    break;

                case 5:
                    showComingSoon("Reports");
                    break;

                case 6:
                    System.out.println("Logged out successfully.");
                    System.out.println();
                    return true;

                case 7:
                    System.out.println("Thank you for using LoanLedger. Goodbye!");
                    System.out.println();
                    return false;

                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 7.");
                    System.out.println();
                    break;
            }
        }
        return false;
    }


    private static void printMenu(String connectionStatus, String username) {
        System.out.println("Logged-in Lender: " + username + " | Database Connection: " + connectionStatus);
        System.out.println();
        System.out.println("--------------- MAIN MENU -----------------");
        System.out.println();
        System.out.println("  1. Borrower Management");
        System.out.println("  2. Loan Management");
        System.out.println("  3. Instalment Management");
        System.out.println("  4. Payment Management");
        System.out.println("  5. Reports");
        System.out.println("  6. Logout");
        System.out.println("  7. Exit");
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
