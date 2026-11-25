import java.math.BigDecimal;
import java.time.LocalDate;

public class Instalment {

    private int id;
    private int loanId;
    private int installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal outstandingAmount;
    private String status; // PENDING, PARTIAL, PAID, OVERDUE

    // Constructor for creating a new instalment
    public Instalment(int loanId, int installmentNumber, LocalDate dueDate,
                      BigDecimal principalDue, BigDecimal interestDue, BigDecimal amountDue,
                      BigDecimal amountPaid, BigDecimal outstandingAmount, String status) {
        this.loanId = loanId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.principalDue = principalDue;
        this.interestDue = interestDue;
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
        this.outstandingAmount = outstandingAmount;
        this.status = status;
    }

    // Constructor when loading from database
    public Instalment(int id, int loanId, int installmentNumber, LocalDate dueDate,
                      BigDecimal principalDue, BigDecimal interestDue, BigDecimal amountDue,
                      BigDecimal amountPaid, BigDecimal outstandingAmount, String status) {
        this.id = id;
        this.loanId = loanId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.principalDue = principalDue;
        this.interestDue = interestDue;
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
        this.outstandingAmount = outstandingAmount;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }

    public int getInstallmentNumber() { return installmentNumber; }
    public void setInstallmentNumber(int installmentNumber) { this.installmentNumber = installmentNumber; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getPrincipalDue() { return principalDue; }
    public void setPrincipalDue(BigDecimal principalDue) { this.principalDue = principalDue; }

    public BigDecimal getInterestDue() { return interestDue; }
    public void setInterestDue(BigDecimal interestDue) { this.interestDue = interestDue; }

    public BigDecimal getAmountDue() { return amountDue; }
    public void setAmountDue(BigDecimal amountDue) { this.amountDue = amountDue; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Instalment #" + installmentNumber + " (ID: " + id + ") | Loan #" + loanId
                + " | Due: " + dueDate + " | Amount: " + amountDue + " | Status: " + status;
    }
}
