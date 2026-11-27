import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InstalmentService {

    private final int lenderId;
    private final LoanService loanService;

    public InstalmentService(int lenderId) {
        this.lenderId = lenderId;
        this.loanService = new LoanService(lenderId);
    }

    public InstalmentService() {
        this(0);
    }

    public int getLenderId() {
        return lenderId;
    }

    public List<Instalment> generateInstalments(int loanId) throws SQLException {
        // Enforces that loan belongs to the logged-in lender
        Loan loan = loanService.getLoanById(loanId);

        if (!loan.isActive()) {
            throw new IllegalArgumentException(
                    "Instalments can only be generated for an active loan. Current status: " + loan.getStatus());
        }

        if (loan.getDurationMonths() <= 0) {
            throw new IllegalArgumentException("Loan duration must be greater than zero.");
        }

        if (loan.getStartDate() == null) {
            throw new IllegalArgumentException("Loan start date is missing.");
        }

        List<Instalment> instalments = buildMonthlySchedule(loan);

        String checkSql = "SELECT COUNT(*) FROM instalments i JOIN loans l ON i.loan_id = l.id " +
                "WHERE i.loan_id = ? AND l.lender_id = ?";
        String insertSql = "INSERT INTO instalments " +
                "(loan_id, installment_number, due_date, principal_due, interest_due, " +
                "amount_due, amount_paid, outstanding_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            // Prevent duplicate instalment generation for the same loan
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, loanId);
                checkPs.setInt(2, this.lenderId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalArgumentException(
                                "Instalments have already been generated for Loan ID: " + loanId);
                    }
                }
            }

            conn.setAutoCommit(false);
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                for (Instalment inst : instalments) {
                    insertPs.setInt(1, inst.getLoanId());
                    insertPs.setInt(2, inst.getInstallmentNumber());
                    insertPs.setDate(3, Date.valueOf(inst.getDueDate()));
                    insertPs.setBigDecimal(4, inst.getPrincipalDue());
                    insertPs.setBigDecimal(5, inst.getInterestDue());
                    insertPs.setBigDecimal(6, inst.getAmountDue());
                    insertPs.setBigDecimal(7, inst.getAmountPaid());
                    insertPs.setBigDecimal(8, inst.getOutstandingAmount());
                    insertPs.setString(9, inst.getStatus());
                    insertPs.executeUpdate();

                    try (ResultSet keys = insertPs.getGeneratedKeys()) {
                        if (keys.next()) {
                            inst.setId(keys.getInt(1));
                        }
                    }
                }

                conn.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection is about to close
                }
            }
        }

        return instalments;
    }

    private List<Instalment> buildMonthlySchedule(Loan loan) {
        int months = loan.getDurationMonths();
        BigDecimal monthCount = new BigDecimal(months);

        BigDecimal totalPrincipal = loan.getPrincipal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = loan.getTotalInterest().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPayable = loan.getTotalPayable().setScale(2, RoundingMode.HALF_UP);

        BigDecimal monthlyPrincipal = totalPrincipal.divide(monthCount, 2, RoundingMode.HALF_UP);
        BigDecimal monthlyInterest = totalInterest.divide(monthCount, 2, RoundingMode.HALF_UP);
        BigDecimal zeroPaid = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal sumPrincipal = BigDecimal.ZERO;
        BigDecimal sumPayable = BigDecimal.ZERO;

        List<Instalment> list = new ArrayList<>();

        for (int i = 1; i <= months; i++) {
            LocalDate dueDate = loan.getStartDate().plusMonths(i);
            BigDecimal principalDue;
            BigDecimal interestDue;
            BigDecimal amountDue;

            if (i < months) {
                principalDue = monthlyPrincipal;
                interestDue = monthlyInterest;
                amountDue = principalDue.add(interestDue);

                sumPrincipal = sumPrincipal.add(principalDue);
                sumPayable = sumPayable.add(amountDue);
            } else {
                // Adjust final instalment so the sum equals totalPayable exactly
                principalDue = totalPrincipal.subtract(sumPrincipal);
                amountDue = totalPayable.subtract(sumPayable);
                interestDue = amountDue.subtract(principalDue);
            }

            Instalment inst = new Instalment(
                    loan.getId(),
                    i,
                    dueDate,
                    principalDue,
                    interestDue,
                    amountDue,
                    zeroPaid,
                    amountDue,
                    "PENDING");
            list.add(inst);
        }

        return list;
    }

    public List<Instalment> getAllInstalments() throws SQLException {
        List<Instalment> list = new ArrayList<>();
        String sql = "SELECT i.id, i.loan_id, i.installment_number, i.due_date, i.principal_due, i.interest_due, " +
                     "i.amount_due, i.amount_paid, i.outstanding_amount, i.status " +
                     "FROM instalments i JOIN loans l ON i.loan_id = l.id " +
                     "WHERE l.lender_id = ? " +
                     "ORDER BY i.loan_id ASC, i.installment_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, this.lenderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readInstalment(rs));
                }
            }
        }
        return list;
    }

    public List<Instalment> getInstalmentsByLoanId(int loanId) throws SQLException {
        // Enforce loan ownership
        loanService.getLoanById(loanId);

        List<Instalment> list = new ArrayList<>();
        String sql = "SELECT i.id, i.loan_id, i.installment_number, i.due_date, i.principal_due, i.interest_due, " +
                "i.amount_due, i.amount_paid, i.outstanding_amount, i.status " +
                "FROM instalments i JOIN loans l ON i.loan_id = l.id " +
                "WHERE i.loan_id = ? AND l.lender_id = ? " +
                "ORDER BY i.installment_number ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, loanId);
            ps.setInt(2, this.lenderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readInstalment(rs));
                }
            }
        }
        return list;
    }

    public List<Instalment> getPendingInstalments() throws SQLException {
        List<Instalment> list = new ArrayList<>();
        String sql = "SELECT i.id, i.loan_id, i.installment_number, i.due_date, i.principal_due, i.interest_due, " +
                     "i.amount_due, i.amount_paid, i.outstanding_amount, i.status " +
                     "FROM instalments i JOIN loans l ON i.loan_id = l.id " +
                     "WHERE l.lender_id = ? AND i.outstanding_amount > 0 AND i.status = 'PENDING' " +
                     "ORDER BY i.due_date ASC, i.loan_id ASC, i.installment_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, this.lenderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readInstalment(rs));
                }
            }
        }
        return list;
    }

    public List<Instalment> getOverdueInstalments() throws SQLException {
        List<Instalment> list = new ArrayList<>();
        String sql = "SELECT i.id, i.loan_id, i.installment_number, i.due_date, i.principal_due, i.interest_due, " +
                     "i.amount_due, i.amount_paid, i.outstanding_amount, i.status " +
                     "FROM instalments i JOIN loans l ON i.loan_id = l.id " +
                     "WHERE l.lender_id = ? AND i.due_date < CURRENT_DATE AND i.outstanding_amount > 0 " +
                     "AND i.status <> 'PAID' " +
                     "ORDER BY i.due_date ASC, i.loan_id ASC, i.installment_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, this.lenderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Instalment inst = readInstalment(rs);
                    inst.setStatus("OVERDUE");
                    list.add(inst);
                }
            }
        }
        return list;
    }

    public List<Instalment> getUpcomingInstalments() throws SQLException {
        List<Instalment> list = new ArrayList<>();
        String sql = "SELECT i.id, i.loan_id, i.installment_number, i.due_date, i.principal_due, i.interest_due, " +
                     "i.amount_due, i.amount_paid, i.outstanding_amount, i.status " +
                     "FROM instalments i JOIN loans l ON i.loan_id = l.id " +
                     "WHERE l.lender_id = ? AND i.due_date >= CURRENT_DATE AND i.due_date <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) " +
                     "AND i.outstanding_amount > 0 AND i.status <> 'PAID' " +
                     "ORDER BY i.due_date ASC, i.loan_id ASC, i.installment_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, this.lenderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readInstalment(rs));
                }
            }
        }
        return list;
    }

    private Instalment readInstalment(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int loanId = rs.getInt("loan_id");
        int installmentNumber = rs.getInt("installment_number");

        LocalDate dueDate = null;
        if (rs.getDate("due_date") != null) {
            dueDate = rs.getDate("due_date").toLocalDate();
        }

        BigDecimal principalDue = rs.getBigDecimal("principal_due");
        BigDecimal interestDue = rs.getBigDecimal("interest_due");
        BigDecimal amountDue = rs.getBigDecimal("amount_due");
        BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
        BigDecimal outstandingAmount = rs.getBigDecimal("outstanding_amount");
        String status = rs.getString("status");

        return new Instalment(id, loanId, installmentNumber, dueDate, principalDue, interestDue,
                amountDue, amountPaid, outstandingAmount, status);
    }
}
