package com.loanledger.ui;

import com.loanledger.exception.BorrowerNotFoundException;
import com.loanledger.exception.ValidationException;
import com.loanledger.model.Borrower;
import com.loanledger.service.BorrowerService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * BorrowerMenu — Console UI for Borrower Management
 *
 * Responsibilities:
 * - Displays borrower management options.
 * - Prompts user for input and passes data to BorrowerService.
 * - Catches and displays user-friendly validation and database errors.
 * - Contains NO direct SQL queries or database connections.
 */
public class BorrowerMenu {

    private final BorrowerService borrowerService;
    private final Scanner scanner;

    public BorrowerMenu(BorrowerService borrowerService, Scanner scanner) {
        this.borrowerService = borrowerService;
        this.scanner = scanner;
    }

    /**
     * Entry point to show and handle the borrower management menu.
     */
    public void display() {
        boolean back = false;

        while (!back) {
            printMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();
            System.out.println();

            int choice = parseChoice(input);

            switch (choice) {
                case 1:
                    handleRegisterBorrower();
                    break;
                case 2:
                    handleViewAllBorrowers();
                    break;
                case 3:
                    handleSearchBorrower();
                    break;
                case 4:
                    handleUpdateBorrower();
                    break;
                case 5:
                    handleDeactivateBorrower();
                    break;
                case 6:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please select an option between 1 and 6.");
                    System.out.println();
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println("============================================");
        System.out.println("           BORROWER MANAGEMENT             ");
        System.out.println("============================================");
        System.out.println("  1. Register New Borrower");
        System.out.println("  2. View All Active Borrowers");
        System.out.println("  3. Search Borrower (by Name, ID, or Phone)");
        System.out.println("  4. Update Borrower Contact Info");
        System.out.println("  5. Deactivate Borrower (Soft Delete)");
        System.out.println("  6. Return to Main Menu");
        System.out.println("============================================");
    }

    /**
     * Option 1: Register New Borrower
     */
    private void handleRegisterBorrower() {
        System.out.println("--- Register New Borrower ---");

        System.out.print("Enter Full Name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter Address: ");
        String address = scanner.nextLine().trim();

        System.out.print("Enter Government ID Number: ");
        String idNumber = scanner.nextLine().trim();

        try {
            Borrower borrower = new Borrower(fullName, phone, address, idNumber);
            Borrower registered = borrowerService.registerBorrower(borrower);

            System.out.println();
            System.out.println("Borrower registered successfully!");
            System.out.println("Generated Borrower ID: " + registered.getId());
            System.out.println();

        } catch (ValidationException e) {
            System.out.println();
            System.out.println("[Validation Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println();
            System.out.println("[Database Error] Could not save borrower: " + e.getMessage());
            System.out.println();
        }
    }

    /**
     * Option 2: View All Active Borrowers
     */
    private void handleViewAllBorrowers() {
        System.out.println("--- Active Borrowers ---");
        try {
            List<Borrower> borrowers = borrowerService.getAllActiveBorrowers();
            printBorrowerTable(borrowers);
        } catch (SQLException e) {
            System.out.println("[Database Error] Could not retrieve borrowers: " + e.getMessage());
            System.out.println();
        }
    }

    /**
     * Option 3: Search Borrower
     */
    private void handleSearchBorrower() {
        System.out.println("--- Search Borrower ---");
        System.out.print("Enter search query (Name, ID, or Phone): ");
        String query = scanner.nextLine().trim();

        try {
            List<Borrower> results = borrowerService.searchBorrowers(query);
            printBorrowerTable(results);
        } catch (SQLException e) {
            System.out.println("[Database Error] Search query failed: " + e.getMessage());
            System.out.println();
        }
    }

    /**
     * Option 4: Update Borrower Contact Info
     */
    private void handleUpdateBorrower() {
        System.out.println("--- Update Borrower ---");
        System.out.print("Enter Borrower ID to update: ");
        String idInput = scanner.nextLine().trim();
        int id = parseChoice(idInput);

        if (id <= 0) {
            System.out.println("Invalid Borrower ID.");
            System.out.println();
            return;
        }

        try {
            Borrower existing = borrowerService.getBorrowerById(id);
            System.out.println("Current Details: " + existing.getFullName() + " | Phone: " + existing.getPhone() +
                    " | Address: " + existing.getAddress() + " | ID: " + existing.getIdNumber());
            System.out.println("(Press Enter to keep existing value)");

            System.out.print("Enter New Full Name [" + existing.getFullName() + "]: ");
            String fullName = scanner.nextLine().trim();
            if (fullName.isEmpty()) fullName = existing.getFullName();

            System.out.print("Enter New Phone Number [" + existing.getPhone() + "]: ");
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty()) phone = existing.getPhone();

            System.out.print("Enter New Address [" + existing.getAddress() + "]: ");
            String address = scanner.nextLine().trim();
            if (address.isEmpty()) address = existing.getAddress();

            System.out.print("Enter New Government ID [" + existing.getIdNumber() + "]: ");
            String idNumber = scanner.nextLine().trim();
            if (idNumber.isEmpty()) idNumber = existing.getIdNumber();

            Borrower updated = new Borrower(existing.getId(), fullName, phone, address, idNumber,
                    existing.getDateRegistered(), existing.getStatus(), existing.getCreatedAt(), null);

            boolean success = borrowerService.updateBorrower(updated);
            if (success) {
                System.out.println();
                System.out.println("Borrower ID " + id + " updated successfully!");
                System.out.println();
            }

        } catch (BorrowerNotFoundException | ValidationException e) {
            System.out.println();
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println();
            System.out.println("[Database Error] Update failed: " + e.getMessage());
            System.out.println();
        }
    }

    /**
     * Option 5: Deactivate Borrower (Soft Delete)
     */
    private void handleDeactivateBorrower() {
        System.out.println("--- Deactivate Borrower ---");
        System.out.print("Enter Borrower ID to deactivate: ");
        String idInput = scanner.nextLine().trim();
        int id = parseChoice(idInput);

        if (id <= 0) {
            System.out.println("Invalid Borrower ID.");
            System.out.println();
            return;
        }

        try {
            Borrower borrower = borrowerService.getBorrowerById(id);
            if (!borrower.isActive()) {
                System.out.println("Borrower ID " + id + " is already inactive.");
                System.out.println();
                return;
            }

            System.out.print("Are you sure you want to deactivate " + borrower.getFullName() + "? (y/n): ");
            String confirm = scanner.nextLine().trim();

            if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
                boolean success = borrowerService.deactivateBorrower(id);
                if (success) {
                    System.out.println("Borrower ID " + id + " has been deactivated (status set to inactive).");
                }
            } else {
                System.out.println("Deactivation cancelled.");
            }
            System.out.println();

        } catch (BorrowerNotFoundException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] Deactivation failed: " + e.getMessage());
            System.out.println();
        }
    }

    /**
     * Formats and prints a list of borrowers in a clean console table.
     */
    private void printBorrowerTable(List<Borrower> borrowers) {
        if (borrowers == null || borrowers.isEmpty()) {
            System.out.println("No borrowers found.");
            System.out.println();
            return;
        }

        System.out.println("+-----+----------------------+-----------------+----------------------+---------------+------------+----------+");
        System.out.printf("| %-3s | %-20s | %-15s | %-20s | %-13s | %-10s | %-8s |%n",
                "ID", "Full Name", "Phone", "Address", "Gov ID", "Registered", "Status");
        System.out.println("+-----+----------------------+-----------------+----------------------+---------------+------------+----------+");

        for (Borrower b : borrowers) {
            String shortAddr = b.getAddress().length() > 20 ? b.getAddress().substring(0, 17) + "..." : b.getAddress();
            String shortName = b.getFullName().length() > 20 ? b.getFullName().substring(0, 17) + "..." : b.getFullName();
            System.out.printf("| %-3d | %-20s | %-15s | %-20s | %-13s | %-10s | %-8s |%n",
                    b.getId(),
                    shortName,
                    b.getPhone(),
                    shortAddr,
                    b.getIdNumber(),
                    b.getDateRegistered() != null ? b.getDateRegistered().toString() : "-",
                    b.getStatus());
        }
        System.out.println("+-----+----------------------+-----------------+----------------------+---------------+------------+----------+");
        System.out.println("Total borrowers displayed: " + borrowers.size());
        System.out.println();
    }

    private int parseChoice(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

