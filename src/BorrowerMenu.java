import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class BorrowerMenu {

    private BorrowerService borrowerService;
    private Scanner scanner;

    public BorrowerMenu(BorrowerService borrowerService, Scanner scanner) {
        this.borrowerService = borrowerService;
        this.scanner = scanner;
    }

    public void display() {
        boolean back = false;

        while (!back) {
            printMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();
            System.out.println();

            int choice = toInt(input);

            switch (choice) {
                case 1:
                    handleAddBorrower();
                    break;
                case 2:
                    handleViewBorrowers();
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
                    System.out.println("Invalid choice. Enter a number between 1 and 6.");
                    System.out.println();
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println("============================================");
        System.out.println("           BORROWER MANAGEMENT             ");
        System.out.println("============================================");
        System.out.println("  1. Add New Borrower");
        System.out.println("  2. View All Active Borrowers");
        System.out.println("  3. Search Borrower");
        System.out.println("  4. Update Borrower");
        System.out.println("  5. Deactivate Borrower");
        System.out.println("  6. Return to Main Menu");
        System.out.println("============================================");
    }

    private void handleAddBorrower() {
        System.out.println("--- Add New Borrower ---");

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
            Borrower saved = borrowerService.registerBorrower(borrower);
            System.out.println("Borrower added successfully! Borrower ID: " + saved.getId());
            System.out.println();

        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewBorrowers() {
        System.out.println("--- Active Borrowers ---");
        try {
            List<Borrower> borrowers = borrowerService.getAllActiveBorrowers();
            printBorrowerTable(borrowers);
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleSearchBorrower() {
        System.out.println("--- Search Borrower ---");
        System.out.print("Enter search query (Name, ID Number, or Phone): ");
        String query = scanner.nextLine().trim();

        try {
            List<Borrower> results = borrowerService.searchBorrowers(query);
            printBorrowerTable(results);
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleUpdateBorrower() {
        System.out.println("--- Update Borrower ---");
        System.out.print("Enter Borrower ID: ");
        int id = toInt(scanner.nextLine().trim());

        if (id <= 0) {
            System.out.println("Invalid Borrower ID.");
            System.out.println();
            return;
        }

        try {
            Borrower existing = borrowerService.getBorrowerById(id);
            System.out.println("Current: " + existing.getFullName() + " | " + existing.getPhone()
                    + " | " + existing.getAddress() + " | " + existing.getIdNumber());
            System.out.println("(Press Enter to keep the current value)");

            System.out.print("New Full Name [" + existing.getFullName() + "]: ");
            String fullName = scanner.nextLine().trim();
            if (fullName.isEmpty())
                fullName = existing.getFullName();

            System.out.print("New Phone [" + existing.getPhone() + "]: ");
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty())
                phone = existing.getPhone();

            System.out.print("New Address [" + existing.getAddress() + "]: ");
            String address = scanner.nextLine().trim();
            if (address.isEmpty())
                address = existing.getAddress();

            System.out.print("New Government ID [" + existing.getIdNumber() + "]: ");
            String idNumber = scanner.nextLine().trim();
            if (idNumber.isEmpty())
                idNumber = existing.getIdNumber();

            Borrower updated = new Borrower(id, fullName, phone, address, idNumber,
                    existing.getDateRegistered(), existing.getStatus(), existing.getCreatedAt(), null);

            borrowerService.updateBorrower(updated);
            System.out.println("Borrower ID " + id + " updated successfully!");
            System.out.println();

        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleDeactivateBorrower() {
        System.out.println("--- Deactivate Borrower ---");
        System.out.print("Enter Borrower ID: ");
        int id = toInt(scanner.nextLine().trim());

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

            System.out.print("Deactivate " + borrower.getFullName() + "? (y/n): ");
            String confirm = scanner.nextLine().trim();

            if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
                borrowerService.deactivateBorrower(id);
                System.out.println("Borrower ID " + id + " has been deactivated.");
            } else {
                System.out.println("Cancelled.");
            }
            System.out.println();

        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

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
            System.out.printf("| %-3d | %-20s | %-15s | %-20s | %-13s | %-10s | %-8s |%n",
                    b.getId(),
                    shorten(b.getFullName(), 20),
                    b.getPhone(),
                    shorten(b.getAddress(), 20),
                    b.getIdNumber(),
                    b.getDateRegistered() != null ? b.getDateRegistered().toString() : "-",
                    b.getStatus());
        }

        System.out.println("+-----+----------------------+-----------------+----------------------+---------------+------------+----------+");
        System.out.println("Total: " + borrowers.size() + " borrower(s)");
        System.out.println();
    }

    private String shorten(String text, int max) {
        if (text == null)
            return "";
        return text.length() > max ? text.substring(0, max - 3) + "..." : text;
    }

    private int toInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
