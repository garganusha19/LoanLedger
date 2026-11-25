import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanService {

    public Loan createLoan(Loan loan) throws SQLException {
        InputValidator.validatePrincipal(loan.getPrincipal());
        InputValidator.validateInterestRate(loan.getInterestRate());
        InputValidator.validateDurationMonths(loan.getDurationMonths());

        String sql = "INSERT INTO loans (borrower_id, principal, interest_type, interest_rate, rate_period, " +
                     "duration_months, total_interest, total_payable, outstanding_balance, " +
                     "start_date, due_date, status, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, loan.getBorrowerId());
            ps.setBigDecimal(2, loan.getPrincipal());
            ps.setString(3, loan.getInterestType());
            ps.setBigDecimal(4, loan.getInterestRate());
            ps.setString(5, loan.getRatePeriod());
            ps.setInt(6, loan.getDurationMonths());
            ps.setBigDecimal(7, loan.getTotalInterest());
            ps.setBigDecimal(8, loan.getTotalPayable());
            ps.setBigDecimal(9, loan.getOutstandingBalance());
            ps.setDate(10, Date.valueOf(loan.getStartDate()));
            ps.setDate(11, Date.valueOf(loan.getDueDate()));
            ps.setString(12, "active");
            ps.setString(13, loan.getRemarks());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                loan.setId(keys.getInt(1));
            }
        }

        return loan;
    }

    public List<Loan> getAllLoans() throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.id, l.borrower_id, b.full_name AS borrower_name, l.principal, " +
                     "l.interest_type, l.interest_rate, l.rate_period, l.duration_months, " +
                     "l.total_interest, l.total_payable, l.outstanding_balance, " +
                     "l.start_date, l.due_date, l.status, l.remarks, l.created_at, l.updated_at " +
                     "FROM loans l LEFT JOIN borrowers b ON l.borrower_id = b.id " +
                     "ORDER BY l.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(readLoan(rs));
            }
        }
        return list;
    }

    public Loan getLoanById(int id) throws SQLException {
        String sql = "SELECT l.id, l.borrower_id, b.full_name AS borrower_name, l.principal, " +
                     "l.interest_type, l.interest_rate, l.rate_period, l.duration_months, " +
                     "l.total_interest, l.total_payable, l.outstanding_balance, " +
                     "l.start_date, l.due_date, l.status, l.remarks, l.created_at, l.updated_at " +
                     "FROM loans l LEFT JOIN borrowers b ON l.borrower_id = b.id " +
                     "WHERE l.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return readLoan(rs);
            } else {
                throw new IllegalArgumentException("Loan not found with ID: " + id);
            }
        }
    }

    public List<Loan> getLoansByBorrowerId(int borrowerId) throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.id, l.borrower_id, b.full_name AS borrower_name, l.principal, " +
                     "l.interest_type, l.interest_rate, l.rate_period, l.duration_months, " +
                     "l.total_interest, l.total_payable, l.outstanding_balance, " +
                     "l.start_date, l.due_date, l.status, l.remarks, l.created_at, l.updated_at " +
                     "FROM loans l LEFT JOIN borrowers b ON l.borrower_id = b.id " +
                     "WHERE l.borrower_id = ? ORDER BY l.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, borrowerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(readLoan(rs));
            }
        }
        return list;
    }

    public List<Loan> getAllActiveLoans() throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.id, l.borrower_id, b.full_name AS borrower_name, l.principal, " +
                     "l.interest_type, l.interest_rate, l.rate_period, l.duration_months, " +
                     "l.total_interest, l.total_payable, l.outstanding_balance, " +
                     "l.start_date, l.due_date, l.status, l.remarks, l.created_at, l.updated_at " +
                     "FROM loans l LEFT JOIN borrowers b ON l.borrower_id = b.id " +
                     "WHERE l.status = 'active' ORDER BY l.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(readLoan(rs));
            }
        }
        return list;
    }

    public List<Loan> getAllCompletedLoans() throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.id, l.borrower_id, b.full_name AS borrower_name, l.principal, " +
                     "l.interest_type, l.interest_rate, l.rate_period, l.duration_months, " +
                     "l.total_interest, l.total_payable, l.outstanding_balance, " +
                     "l.start_date, l.due_date, l.status, l.remarks, l.created_at, l.updated_at " +
                     "FROM loans l LEFT JOIN borrowers b ON l.borrower_id = b.id " +
                     "WHERE l.status = 'completed' ORDER BY l.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(readLoan(rs));
            }
        }
        return list;
    }

    public void updateLoanStatus(int loanId, String newStatus) throws SQLException {
        getLoanById(loanId);

        String sql = "UPDATE loans SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, loanId);
            ps.executeUpdate();
        }
    }

    public BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal interestRate,
                                              String ratePeriod, String interestType,
                                              int durationMonths) {

        BigDecimal rateDecimal = interestRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        if ("reducing_balance".equalsIgnoreCase(interestType)) {
            BigDecimal monthlyRate;
            if ("annually".equalsIgnoreCase(ratePeriod)) {
                monthlyRate = rateDecimal.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            } else if ("weekly".equalsIgnoreCase(ratePeriod)) {
                monthlyRate = rateDecimal.multiply(new BigDecimal("52")).divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            } else if ("daily".equalsIgnoreCase(ratePeriod)) {
                monthlyRate = rateDecimal.multiply(new BigDecimal("365")).divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            } else {
                monthlyRate = rateDecimal;
            }

            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal power = BigDecimal.ONE;
            for (int i = 0; i < durationMonths; i++) {
                power = power.multiply(onePlusR);
            }

            BigDecimal emi = principal.multiply(monthlyRate).multiply(power)
                    .divide(power.subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);

            BigDecimal totalPaid = emi.multiply(new BigDecimal(durationMonths));
            return totalPaid.subtract(principal).setScale(2, RoundingMode.HALF_UP);

        } else {
            BigDecimal numberOfPeriods;
            BigDecimal months = new BigDecimal(durationMonths);

            if ("annually".equalsIgnoreCase(ratePeriod)) {
                numberOfPeriods = months.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            } else if ("weekly".equalsIgnoreCase(ratePeriod)) {
                numberOfPeriods = months.multiply(new BigDecimal("52")).divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            } else if ("daily".equalsIgnoreCase(ratePeriod)) {
                numberOfPeriods = months.multiply(new BigDecimal("365")).divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            } else {
                numberOfPeriods = months;
            }

            return principal.multiply(rateDecimal).multiply(numberOfPeriods).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private Loan readLoan(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int borrowerId = rs.getInt("borrower_id");
        String borrowerName = rs.getString("borrower_name");
        BigDecimal principal = rs.getBigDecimal("principal");
        String interestType = rs.getString("interest_type");
        BigDecimal interestRate = rs.getBigDecimal("interest_rate");
        String ratePeriod = rs.getString("rate_period");
        int durationMonths = rs.getInt("duration_months");
        BigDecimal totalInterest = rs.getBigDecimal("total_interest");
        BigDecimal totalPayable = rs.getBigDecimal("total_payable");
        BigDecimal outstandingBalance = rs.getBigDecimal("outstanding_balance");
        String status = rs.getString("status");
        String remarks = rs.getString("remarks");

        LocalDate startDate = null;
        if (rs.getDate("start_date") != null) {
            startDate = rs.getDate("start_date").toLocalDate();
        }

        LocalDate dueDate = null;
        if (rs.getDate("due_date") != null) {
            dueDate = rs.getDate("due_date").toLocalDate();
        }

        LocalDateTime createdAt = null;
        if (rs.getTimestamp("created_at") != null) {
            createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        }

        LocalDateTime updatedAt = null;
        if (rs.getTimestamp("updated_at") != null) {
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        }

        return new Loan(id, borrowerId, borrowerName, principal, interestType, interestRate,
                ratePeriod, durationMonths, totalInterest, totalPayable, outstandingBalance,
                startDate, dueDate, status, remarks, createdAt, updatedAt);
    }
}
