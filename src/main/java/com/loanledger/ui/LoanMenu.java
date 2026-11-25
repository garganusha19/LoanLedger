package com.loanledger.ui;

import com.loanledger.exception.BorrowerNotFoundException;
import com.loanledger.exception.LoanNotFoundException;
import com.loanledger.exception.ValidationException;
import com.loanledger.model.Borrower;
import com.loanledger.model.Loan;
import com.loanledger.service.BorrowerService;
import com.loanledger.service.LoanService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class LoanMenu {

    private final LoanService loanService;
    private final BorrowerService borrowerService;
    private final Scanner scanner;

    public LoanMenu(LoanService loanService, BorrowerService borrowerService, Scanner scanner) {
        this.loanService = loanService;
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

            int choice = parseChoice(input);

            switch (choice) {
                case 1:
                    handleCreateLoan();
                    break;
                case 2:
                    handleViewAllLoans();
                    break;
                case 3:
                    handleSearchByBorrower();
                    break;
                case 4:
                    handleViewLoanDetails();
                    break;
                case 5:
                    handleViewActiveLoans();
                    break;
                case 6:
                    handleViewCompletedLoans();
                    break;
                case 7:
                    handleUpdateLoanStatus();
                    break;
                case 8:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please select an option between 1 and 8.");
                    System.out.println();
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println("============================================");
        System.out.println("             LOAN MANAGEMENT               ");
        System.out.println("============================================");
        System.out.println("  1. Create New Loan");
        System.out.println("  2. View All Loans");
        System.out.println("  3. Search Loans by Borrower");
        System.out.println("  4. View Loan Details");
        System.out.println("  5. View Active Loans");
        System.out.println("  6. View Completed Loans");
        System.out.println("  7. Update Loan Status");
        System.out.println("  8. Return to Main Menu");
        System.out.println("============================================");
    }

    private void handleCreateLoan() {
        System.out.println("--- Create New Loan ---");

        try {
            List<Borrower> activeBorrowers = borrowerService.getAllActiveBorrowers();
            if (activeBorrowers.isEmpty()) {
                System.out.println("No active borrowers found. Please register a borrower first.");
                System.out.println();
                return;
            }
            System.out.println("Active Borrowers:");
            printBorrowerList(activeBorrowers);
        } catch (SQLException e) {
            System.out.println("[Database Error] Could not load borrowers: " + e.getMessage());
            System.out.println();
            return;
        }

        System.out.print("Enter Borrower ID: ");
        int borrowerId = parseChoice(scanner.nextLine().trim());
        if (borrowerId <= 0) {
            System.out.println("Invalid Borrower ID.");
            System.out.println();
            return;
        }

        Borrower borrower;
        try {
            borrower = borrowerService.getBorrowerById(borrowerId);
            if (!borrower.isActive()) {
                System.out.println("[Error] Borrower ID " + borrowerId + " is inactive and cannot take a new loan.");
                System.out.println();
                return;
            }
            System.out.println("Selected Borrower: " + borrower.getFullName());
            System.out.println();
        } catch (BorrowerNotFoundException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
            return;
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
            return;
        }

        BigDecimal principal = readBigDecimal("Enter Principal Amount (e.g. 50000): ");

        System.out.println("Interest Types:");
        System.out.println("  1. Flat");
        System.out.println("  2. Simple");
        System.out.println("  3. Reducing Balance");
        System.out.print("Select Interest Type (1-3): ");
        int typeChoice = parseChoice(scanner.nextLine().trim());
        String interestType;
        if (typeChoice == 1) {
            interestType = "flat";
        } else if (typeChoice == 2) {
            interestType = "simple";
        } else if (typeChoice == 3) {
            interestType = "reducing_balance";
        } else {
            System.out.println("Invalid selection. Defaulting to Flat.");
            interestType = "flat";
        }

        BigDecimal interestRate = readBigDecimal("Enter Interest Rate (%) (e.g. 5.00): ");

        System.out.println("Rate Period:");
        System.out.println("  1. Daily");
        System.out.println("  2. Weekly");
        System.out.println("  3. Monthly");
        System.out.println("  4. Annually");
        System.out.print("Select Rate Period (1-4): ");
        int periodChoice = parseChoice(scanner.nextLine().trim());
        String ratePeriod;
        if (periodChoice == 1) {
            ratePeriod = "daily";
        } else if (periodChoice == 2) {
            ratePeriod = "weekly";
        } else if (periodChoice == 3) {
            ratePeriod = "monthly";
        } else if (periodChoice == 4) {
            ratePeriod = "annually";
        } else {
            System.out.println("Invalid selection. Defaulting to Monthly.");
            ratePeriod = "monthly";
        }

        System.out.print("Enter Loan Duration (months, e.g. 12): ");
        int durationMonths = parseChoice(scanner.nextLine().trim());
        if (durationMonths <= 0) {
            System.out.println("Invalid duration.");
            System.out.println();
            return;
        }

        LocalDate startDate = readDate("Enter Start Date [YYYY-MM-DD] (press Enter for today): ", LocalDate.now());
        LocalDate dueDate = startDate.plusMonths(durationMonths);
        System.out.println("Calculated Due Date: " + dueDate);

        System.out.print("Enter Remarks (optional, press Enter to skip): ");
        String remarks = scanner.nextLine().trim();
        if (remarks.isEmpty()) {
            remarks = null;
        }

        BigDecimal totalInterest;
        try {
            totalInterest = loanService.calculateTotalInterest(principal, interestRate, ratePeriod, interestType, durationMonths);
        } catch (Exception e) {
            System.out.println("[Error] Could not calculate interest: " + e.getMessage());
            System.out.println();
            return;
        }

        BigDecimal totalPayable = principal.add(totalInterest).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyInstallment = totalPayable.divide(new BigDecimal(durationMonths), 2, RoundingMode.HALF_UP);

        System.out.println();
        System.out.println("--- Loan Summary ---");
        System.out.println("Borrower       : " + borrower.getFullName() + " (ID: " + borrowerId + ")");
        System.out.println("Principal      : " + formatMoney(principal));
        System.out.println("Interest Type  : " + formatInterestType(interestType));
        System.out.println("Interest Rate  : " + interestRate + "% per " + formatRatePeriod(ratePeriod));
        System.out.println("Duration       : " + durationMonths + " month(s)");
        System.out.println("Total Interest : " + formatMoney(totalInterest));
        System.out.println("Total Payable  : " + formatMoney(totalPayable));
        System.out.println("Est. Monthly   : " + formatMoney(monthlyInstallment));
        System.out.println("Start Date     : " + startDate);
        System.out.println("Due Date       : " + dueDate);

        if ("reducing_balance".equalsIgnoreCase(interestType)) {
            System.out.println();
            System.out.println("[Note] Reducing balance total is an estimate based on the amortization formula.");
            System.out.println("       Actual interest depends on the payment schedule.");
        }

        System.out.println();
        System.out.print("Confirm and save this loan? (y/n): ");
        String confirm = scanner.nextLine().trim();

        if (!"y".equalsIgnoreCase(confirm) && !"yes".equalsIgnoreCase(confirm)) {
            System.out.println("Loan creation cancelled.");
            System.out.println();
            return;
        }

        try {
            Loan loan = new Loan(borrowerId, principal, interestType, interestRate, ratePeriod,
                    durationMonths, totalInterest, totalPayable, totalPayable, startDate, dueDate, remarks);

            Loan saved = loanService.createLoan(loan);
            System.out.println();
            System.out.println("Loan created successfully! Loan ID: " + saved.getId());
            System.out.println();

        } catch (ValidationException e) {
            System.out.println("[Validation Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] Could not save loan: " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewAllLoans() {
        System.out.println("--- All Loans ---");
        try {
            List<Loan> loans = loanService.getAllLoans();
            printLoanTable(loans);
        } catch (SQLException e) {
            System.out.println("[Database Error] Could not retrieve loans: " + e.getMessage());
            System.out.println();
        }
    }

    private void handleSearchByBorrower() {
        System.out.println("--- Search Loans by Borrower ---");
        System.out.print("Enter Borrower ID: ");
        int borrowerId = parseChoice(scanner.nextLine().trim());

        if (borrowerId <= 0) {
            System.out.println("Invalid Borrower ID.");
            System.out.println();
            return;
        }

        try {
            List<Loan> loans = loanService.getLoansByBorrowerId(borrowerId);
            if (loans.isEmpty()) {
                System.out.println("No loans found for Borrower ID " + borrowerId + ".");
                System.out.println();
            } else {
                System.out.println("Loans for Borrower ID " + borrowerId + ":");
                printLoanTable(loans);
            }
        } catch (ValidationException e) {
            System.out.println("[Validation Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewLoanDetails() {
        System.out.println("--- Loan Details ---");
        System.out.print("Enter Loan ID: ");
        int loanId = parseChoice(scanner.nextLine().trim());

        if (loanId <= 0) {
            System.out.println("Invalid Loan ID.");
            System.out.println();
            return;
        }

        try {
            Loan loan = loanService.getLoanById(loanId);
            printLoanDetails(loan);
        } catch (LoanNotFoundException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewActiveLoans() {
        System.out.println("--- Active Loans ---");
        try {
            List<Loan> loans = loanService.getAllActiveLoans();
            printLoanTable(loans);
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleViewCompletedLoans() {
        System.out.println("--- Completed Loans ---");
        try {
            List<Loan> loans = loanService.getAllCompletedLoans();
            printLoanTable(loans);
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void handleUpdateLoanStatus() {
        System.out.println("--- Update Loan Status ---");
        System.out.print("Enter Loan ID: ");
        int loanId = parseChoice(scanner.nextLine().trim());

        if (loanId <= 0) {
            System.out.println("Invalid Loan ID.");
            System.out.println();
            return;
        }

        try {
            Loan loan = loanService.getLoanById(loanId);
            System.out.println("Loan ID     : " + loan.getId());
            System.out.println("Borrower    : " + (loan.getBorrowerName() != null ? loan.getBorrowerName() : "ID " + loan.getBorrowerId()));
            System.out.println("Current Status: " + loan.getStatus());
            System.out.println();

            System.out.println("New Status:");
            System.out.println("  1. Active");
            System.out.println("  2. Completed");
            System.out.println("  3. Defaulted");
            System.out.print("Select new status (1-3): ");
            int statusChoice = parseChoice(scanner.nextLine().trim());

            String newStatus;
            if (statusChoice == 1) {
                newStatus = "active";
            } else if (statusChoice == 2) {
                newStatus = "completed";
            } else if (statusChoice == 3) {
                newStatus = "defaulted";
            } else {
                System.out.println("Invalid selection. No changes made.");
                System.out.println();
                return;
            }

            System.out.print("Change status to '" + newStatus + "'? (y/n): ");
            String confirm = scanner.nextLine().trim();

            if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
                loanService.updateLoanStatus(loanId, newStatus);
                System.out.println("Loan ID " + loanId + " status updated to '" + newStatus + "'.");
            } else {
                System.out.println("Update cancelled.");
            }
            System.out.println();

        } catch (LoanNotFoundException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println();
        } catch (ValidationException e) {
            System.out.println("[Validation Error] " + e.getMessage());
            System.out.println();
        } catch (SQLException e) {
            System.out.println("[Database Error] " + e.getMessage());
            System.out.println();
        }
    }

    private void printLoanTable(List<Loan> loans) {
        if (loans == null || loans.isEmpty()) {
            System.out.println("No loans found.");
            System.out.println();
            return;
        }

        System.out.println("+-----+----------------------+--------------+--------------+------+----------+--------+--------------+-----------+");
        System.out.printf("| %-3s | %-20s | %-12s | %-12s | %-4s | %-8s | %-6s | %-12s | %-9s |%n",
                "ID", "Borrower", "Principal", "Total Payable", "Rate", "Period", "Months", "Due Date", "Status");
        System.out.println("+-----+----------------------+--------------+--------------+------+----------+--------+--------------+-----------+");

        for (Loan l : loans) {
            String name = l.getBorrowerName() != null ? l.getBorrowerName() : "ID:" + l.getBorrowerId();
            System.out.printf("| %-3d | %-20s | %-12s | %-12s | %-4s | %-8s | %-6d | %-12s | %-9s |%n",
                    l.getId(),
                    truncate(name, 20),
                    formatMoney(l.getPrincipal()),
                    formatMoney(l.getTotalPayable()),
                    l.getInterestRate().stripTrailingZeros().toPlainString() + "%",
                    formatRatePeriod(l.getRatePeriod()),
                    l.getDurationMonths(),
                    l.getDueDate() != null ? l.getDueDate().toString() : "-",
                    l.getStatus());
        }

        System.out.println("+-----+----------------------+--------------+--------------+------+----------+--------+--------------+-----------+");
        System.out.println("Total loans displayed: " + loans.size());
        System.out.println();
    }

    private void printLoanDetails(Loan l) {
        BigDecimal monthlyInstallment = l.getTotalPayable().divide(
                new BigDecimal(l.getDurationMonths()), 2, RoundingMode.HALF_UP);

        String name = l.getBorrowerName() != null ? l.getBorrowerName() : "ID: " + l.getBorrowerId();

        System.out.println("============================================");
        System.out.println("             LOAN DETAILS                  ");
        System.out.println("============================================");
        System.out.println("Loan ID           : " + l.getId());
        System.out.println("Borrower          : " + name + " (ID: " + l.getBorrowerId() + ")");
        System.out.println("Principal         : " + formatMoney(l.getPrincipal()));
        System.out.println("Interest Type     : " + formatInterestType(l.getInterestType()));
        System.out.println("Interest Rate     : " + l.getInterestRate().stripTrailingZeros().toPlainString()
                + "% per " + formatRatePeriod(l.getRatePeriod()));
        System.out.println("Duration          : " + l.getDurationMonths() + " month(s)");
        System.out.println("Total Interest    : " + formatMoney(l.getTotalInterest()));
        System.out.println("Total Payable     : " + formatMoney(l.getTotalPayable()));
        System.out.println("Outstanding Bal.  : " + formatMoney(l.getOutstandingBalance()));
        System.out.println("Est. Monthly Inst.: " + formatMoney(monthlyInstallment));
        System.out.println("Start Date        : " + (l.getStartDate() != null ? l.getStartDate() : "-"));
        System.out.println("Due Date          : " + (l.getDueDate() != null ? l.getDueDate() : "-"));
        System.out.println("Status            : " + l.getStatus());
        System.out.println("Remarks           : " + (l.getRemarks() != null && !l.getRemarks().isEmpty() ? l.getRemarks() : "-"));
        System.out.println("Created At        : " + (l.getCreatedAt() != null ? l.getCreatedAt() : "-"));
        System.out.println("============================================");
        System.out.println();
    }

    private void printBorrowerList(List<Borrower> borrowers) {
        System.out.println("+-----+----------------------+-----------------+");
        System.out.printf("| %-3s | %-20s | %-15s |%n", "ID", "Full Name", "Phone");
        System.out.println("+-----+----------------------+-----------------+");
        for (Borrower b : borrowers) {
            System.out.printf("| %-3d | %-20s | %-15s |%n",
                    b.getId(),
                    truncate(b.getFullName(), 20),
                    b.getPhone());
        }
        System.out.println("+-----+----------------------+-----------------+");
        System.out.println();
    }

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                BigDecimal value = new BigDecimal(input);
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid numeric value (e.g. 5000.00).");
            }
        }
    }

    private LocalDate readDate(String prompt, LocalDate defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using today's date: " + defaultValue);
            return defaultValue;
        }
    }

    private String formatInterestType(String interestType) {
        if (interestType == null) return "-";
        switch (interestType.toLowerCase()) {
            case "flat":             return "Flat";
            case "simple":           return "Simple";
            case "reducing_balance": return "Reducing Balance";
            default:                 return interestType;
        }
    }

    private String formatRatePeriod(String ratePeriod) {
        if (ratePeriod == null) return "-";
        switch (ratePeriod.toLowerCase()) {
            case "daily":    return "Daily";
            case "weekly":   return "Weekly";
            case "monthly":  return "Monthly";
            case "annually": return "Annually";
            default:         return ratePeriod;
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount.doubleValue());
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 3) + "..." : s;
    }

    private int parseChoice(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
