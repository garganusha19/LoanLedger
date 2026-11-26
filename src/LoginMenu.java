import java.sql.SQLException;
import java.util.Scanner;

public class LoginMenu {

    private final AuthService authService;
    private final Scanner scanner;

    public LoginMenu(AuthService authService, Scanner scanner) {
        this.authService = authService;
        this.scanner = scanner;
    }

    public boolean display() {
        while (true) {
            printAuthMenu();
            String input = scanner.nextLine().trim();
            System.out.println();

            int choice = toNumber(input);

            switch (choice) {
                case 1:
                    boolean success = handleLogin();
                    if (success) {
                        return true;
                    }
                    break;

                case 2:
                    handleSignUp();
                    break;

                case 3:
                    return false;

                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                    System.out.println();
                    break;
            }
        }
    }

    private void printAuthMenu() {
        System.out.println("----------------------------------------------");
        System.out.println("      LOANLEDGER - LENDER AUTHENTICATION      ");
        System.out.println("----------------------------------------------");
        System.out.println();
        System.out.println("  1. Login");
        System.out.println("  2. Sign Up");
        System.out.println("  3. Exit");
        System.out.println();
        System.out.print("Enter your choice: ");
    }

    private boolean handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        System.out.println();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("[Error] Username and password cannot be empty.");
            System.out.println();
            return false;
        }

        try {
            int result = authService.login(username, password);

            if (result == AuthService.LOGIN_SUCCESS) {
                System.out.println("Login successful! Welcome, " + username + ".");
                System.out.println();
                return true;
            } else if (result == AuthService.USER_NOT_FOUND) {
                System.out.println("Account not found. Please sign up first.");
                System.out.println();
                return false;
            } else if (result == AuthService.INVALID_PASSWORD) {
                System.out.println("Invalid password. Please try again.");
                System.out.println();
                return false;
            }

        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
            return false;
        }

        return false;
    }

    private void handleSignUp() {
        System.out.println("--------------- LENDER SIGN UP ---------------");
        System.out.print("Enter a new username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter a password: ");
        String password = scanner.nextLine().trim();
        System.out.println();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("[Error] Username and password cannot be empty.");
            System.out.println();
            return;
        }

        try {
            if (authService.isUsernameTaken(username)) {
                System.out.println("Username already taken. Please choose another username.");
                System.out.println();
                return;
            }

            boolean created = authService.register(username, password);
            if (created) {
                System.out.println("Account created successfully! Please log in.");
                System.out.println();
            } else {
                System.out.println("[Error] Could not create account. Please try again.");
                System.out.println();
            }

        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private int toNumber(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
