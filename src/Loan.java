import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {

    private int id;
    private int borrowerId;
    private String borrowerName;
    private BigDecimal principal;
    private String interestType;      // flat, simple, reducing_balance
    private BigDecimal interestRate;  // e.g. 5.00 means 5%
    private String ratePeriod;        // daily, weekly, monthly, annually
    private int durationMonths;
    private BigDecimal totalInterest;
    private BigDecimal totalPayable;
    private BigDecimal outstandingBalance;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String status;            // active, completed, defaulted
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor for creating a new loan
    public Loan(int borrowerId, BigDecimal principal, String interestType,
                BigDecimal interestRate, String ratePeriod, int durationMonths,
                BigDecimal totalInterest, BigDecimal totalPayable,
                BigDecimal outstandingBalance, LocalDate startDate,
                LocalDate dueDate, String remarks) {
        this.borrowerId = borrowerId;
        this.principal = principal;
        this.interestType = interestType;
        this.interestRate = interestRate;
        this.ratePeriod = ratePeriod;
        this.durationMonths = durationMonths;
        this.totalInterest = totalInterest;
        this.totalPayable = totalPayable;
        this.outstandingBalance = outstandingBalance;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.remarks = remarks;
        this.status = "active";
    }

    // Constructor when loading from database
    public Loan(int id, int borrowerId, String borrowerName, BigDecimal principal,
                String interestType, BigDecimal interestRate, String ratePeriod,
                int durationMonths, BigDecimal totalInterest, BigDecimal totalPayable,
                BigDecimal outstandingBalance, LocalDate startDate, LocalDate dueDate,
                String status, String remarks, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.borrowerName = borrowerName;
        this.principal = principal;
        this.interestType = interestType;
        this.interestRate = interestRate;
        this.ratePeriod = ratePeriod;
        this.durationMonths = durationMonths;
        this.totalInterest = totalInterest;
        this.totalPayable = totalPayable;
        this.outstandingBalance = outstandingBalance;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBorrowerId() { return borrowerId; }
    public void setBorrowerId(int borrowerId) { this.borrowerId = borrowerId; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }

    public String getInterestType() { return interestType; }
    public void setInterestType(String interestType) { this.interestType = interestType; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public String getRatePeriod() { return ratePeriod; }
    public void setRatePeriod(String ratePeriod) { this.ratePeriod = ratePeriod; }

    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }

    public BigDecimal getTotalInterest() { return totalInterest; }
    public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }

    public BigDecimal getTotalPayable() { return totalPayable; }
    public void setTotalPayable(BigDecimal totalPayable) { this.totalPayable = totalPayable; }

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Loan #" + id + " | Borrower #" + borrowerId + " | Principal: " + principal + " | Status: " + status;
    }
}
