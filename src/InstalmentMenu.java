import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class InstalmentMenu {

    private InstalmentService instalmentService;
    private LoanService loanService;
    private Scanner scanner;

    public InstalmentMenu(InstalmentService instalmentService, LoanService loanService, Scanner scanner) {
        this.instalmentService = instalmentService;
        this.loanService = loanService;
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
                    handleGenerateInstalments();
                    break;
                case 2:
                    handleViewAllInstalments();
                    break;
                case 3:
                    handleViewByLoanId();
                    break;
                case 4:
                    handleViewPendingInstalments();
                    break;
                case 5:
                    handleViewOverdueInstalments();
                    break;
                case 6:
                    handleViewUpcomingInstalments();
                    break;
                case 7:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Enter a number between 1 and 7.");
                    System.out.println();
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println("================================");
        System.out.println("       INSTALMENT MANAGEMENT    ");
        System.out.println("================================");
        System.out.println("  1. Generate Instalments for Loan");
        System.out.println("  2. View All Instalments");
        System.out.println("  3. View Instalments by Loan ID");
        System.out.println("  4. View Pending Instalments");
        System.out.println("  5. View Overdue Instalments");
        System.out.println("  6. View Upcoming Instalments");
        System.out.println("  7. Return to Main Menu");
        System.out.println("================================");
    }

    private void handleGenerateInstalments() {
        System.out.println("--- Generate Instalments for Loan ---");

        try {
            List<Loan> activeLoans = loanService.getAllActiveLoans();
            if (activeLoans.isEmpty()) {
                System.out.println("No active loans found. Please create a loan first.");
                System.out.println();
                return;
            }
            printActiveLoans(activeLoans);
        } catch (SQLException e) {
            System.out.println("[Database Error] Could not load active loans: " + e.getMessage());
            System.out.println();
            return;
        }

        System.out.print("Enter Loan ID: ");
        int loanId = toInt(scanner.nextLine().trim());
        if (loanId <= 0) {
            System.out.println("Invalid Loan ID.");
            System.out.println();
            return;
        }

        try {
            List<Instalment> generated = instalmentService.generateInstalments(loanId);
            System.out.println("Generated " + generated.size() + " instalment(s) for Loan ID " + loanId + ".");
            System.out.println();
            printInstalmentTable(generated);
        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewAllInstalments() {
        System.out.println("--- All Instalments ---");
        try {
            List<Instalment> instalments = instalmentService.getAllInstalments();
            printInstalmentTable(instalments);
        } catch (SQLException e) {
            System.out.println("[Database Error] Could not retrieve instalments: " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewByLoanId() {
        System.out.println("--- Instalments by Loan ID ---");
        System.out.print("Enter Loan ID: ");
        int loanId = toInt(scanner.nextLine().trim());

        if (loanId <= 0) {
            System.out.println("Invalid Loan ID.");
            System.out.println();
            return;
        }

        try {
            List<Instalment> instalments = instalmentService.getInstalmentsByLoanId(loanId);
            if (instalments.isEmpty()) {
                System.out.println("No instalments found for Loan ID " + loanId + ".");
                System.out.println();
            } else {
                printInstalmentTable(instalments);
            }
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewPendingInstalments() {
        System.out.println("--- Pending Instalments ---");
        try {
            List<Instalment> instalments = instalmentService.getPendingInstalments();
            printInstalmentTable(instalments);
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewOverdueInstalments() {
        System.out.println("--- Overdue Instalments ---");
        try {
            List<Instalment> instalments = instalmentService.getOverdueInstalments();
            printInstalmentTable(instalments);
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewUpcomingInstalments() {
        System.out.println("--- Upcoming Instalments (Next 30 Days) ---");
        try {
            List<Instalment> instalments = instalmentService.getUpcomingInstalments();
            printInstalmentTable(instalments);
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void printActiveLoans(List<Loan> loans) {
        System.out.println("+-----+----------------------+--------------+--------------+--------+");
        System.out.printf("| %-3s | %-20s | %-12s | %-12s | %-6s |%n",
                "ID", "Borrower", "Principal", "Total Payable", "Months");
        System.out.println("+-----+----------------------+--------------+--------------+--------+");
        for (Loan l : loans) {
            String name = l.getBorrowerName() != null ? l.getBorrowerName() : "ID:" + l.getBorrowerId();
            System.out.printf("| %-3d | %-20s | %-12s | %-12s | %-6d |%n",
                    l.getId(),
                    shorten(name, 20),
                    money(l.getPrincipal()),
                    money(l.getTotalPayable()),
                    l.getDurationMonths());
        }
        System.out.println("+-----+----------------------+--------------+--------------+--------+");
        System.out.println();
    }

    private void printInstalmentTable(List<Instalment> instalments) {
        if (instalments == null || instalments.isEmpty()) {
            System.out.println("No instalments found.");
            System.out.println();
            return;
        }

        System.out.println("+-----+---------+--------+------------+--------------+--------------+--------------+--------------+--------------+----------+");
        System.out.printf("| %-3s | %-7s | %-6s | %-10s | %-12s | %-12s | %-12s | %-12s | %-12s | %-8s |%n",
                "ID", "Loan ID", "Inst #", "Due Date", "Principal", "Interest", "Total Amount", "Paid Amount", "Outstanding", "Status");
        System.out.println("+-----+---------+--------+------------+--------------+--------------+--------------+--------------+--------------+----------+");

        for (Instalment inst : instalments) {
            System.out.printf("| %-3d | %-7d | %-6d | %-10s | %-12s | %-12s | %-12s | %-12s | %-12s | %-8s |%n",
                    inst.getId(),
                    inst.getLoanId(),
                    inst.getInstallmentNumber(),
                    inst.getDueDate() != null ? inst.getDueDate().toString() : "-",
                    money(inst.getPrincipalDue()),
                    money(inst.getInterestDue()),
                    money(inst.getAmountDue()),
                    money(inst.getAmountPaid()),
                    money(inst.getOutstandingAmount()),
                    inst.getStatus());
        }

        System.out.println("+-----+---------+--------+------------+--------------+--------------+--------------+--------------+--------------+----------+");
        System.out.println("Total: " + instalments.size() + " instalment(s)");
        System.out.println();
    }

    private String money(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount.doubleValue());
    }

    private String shorten(String text, int max) {
        if (text == null) return "";
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
