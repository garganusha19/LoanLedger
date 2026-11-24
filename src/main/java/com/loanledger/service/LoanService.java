package com.loanledger.service;

import com.loanledger.exception.LoanNotFoundException;
import com.loanledger.exception.ValidationException;
import com.loanledger.model.Loan;
import com.loanledger.util.DBConnection;
import com.loanledger.util.InputValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LoanService — Service Layer for Loan Management
 *
 * Responsibilities:
 * - Implements business logic and data validation for loans.
 * - Executes direct JDBC operations using PreparedStatement and DBConnection.
 * - Adheres strictly to the architectural rule: NO DAO layer.
 * - Uses BigDecimal for all monetary calculations with RoundingMode.HALF_UP.
 */
public class LoanService {

    // Reused base SELECT for all loan queries — includes JOIN with borrowers for name
    private static final String BASE_SELECT =
            "SELECT l.id, l.borrower_id, b.full_name AS borrower_name, l.principal, " +
            "l.interest_type, l.interest_rate, l.rate_period, l.duration_months, " +
            "l.total_interest, l.total_payable, l.outstanding_balance, " +
            "l.start_date, l.due_date, l.status, l.remarks, l.created_at, l.updated_at " +
            "FROM loans l LEFT JOIN borrowers b ON l.borrower_id = b.id ";

    /**
     * Creates a new loan record in the database.
     *
     * @param loan Loan model with all required fields populated
     * @return the saved Loan with its auto-generated database ID
     * @throws ValidationException if input validation fails
     * @throws SQLException if a database error occurs
     */
    public Loan createLoan(Loan loan) throws SQLException {
        if (loan == null) {
            throw new ValidationException("Loan details cannot be null.");
        }

        // Validate all loan fields
        InputValidator.validatePositiveId(loan.getBorrowerId(), "Borrower ID");
        InputValidator.validatePrincipal(loan.getPrincipal());
        InputValidator.validateInterestType(loan.getInterestType());
        InputValidator.validateInterestRate(loan.getInterestRate());
        InputValidator.validateRatePeriod(loan.getRatePeriod());
        InputValidator.validateDurationMonths(loan.getDurationMonths());

        String sql = "INSERT INTO loans " +
                "(borrower_id, principal, interest_type, interest_rate, rate_period, " +
                "duration_months, total_interest, total_payable, outstanding_balance, " +
                "start_date, due_date, status, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String status = loan.getStatus() != null ? loan.getStatus().toLowerCase() : "active";
            LocalDate startDate = loan.getStartDate() != null ? loan.getStartDate() : LocalDate.now();

            ps.setInt(1, loan.getBorrowerId());
            ps.setBigDecimal(2, loan.getPrincipal());
            ps.setString(3, loan.getInterestType().toLowerCase());
            ps.setBigDecimal(4, loan.getInterestRate());
            ps.setString(5, loan.getRatePeriod().toLowerCase());
            ps.setInt(6, loan.getDurationMonths());
            ps.setBigDecimal(7, loan.getTotalInterest());
            ps.setBigDecimal(8, loan.getTotalPayable());
            ps.setBigDecimal(9, loan.getOutstandingBalance());
            ps.setDate(10, Date.valueOf(startDate));
            ps.setDate(11, Date.valueOf(loan.getDueDate()));
            ps.setString(12, status);
            ps.setString(13, loan.getRemarks());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating loan failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    loan.setId(generatedKeys.getInt(1));
                    loan.setStatus(status);
                    loan.setStartDate(startDate);
                } else {
                    throw new SQLException("Creating loan failed, no ID obtained.");
                }
            }
        }

        return loan;
    }

    /**
     * Retrieves all loans in the system, ordered by most recent first.
     *
     * @return list of all Loan entities
     * @throws SQLException if database read fails
     */
    public List<Loan> getAllLoans() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY l.id DESC";
        return fetchLoans(sql, null);
    }

    /**
     * Retrieves a single loan by its primary ID.
     *
     * @param id Loan primary key
     * @return Loan model
     * @throws LoanNotFoundException if no loan matches the given ID
     * @throws SQLException if database read fails
     */
    public Loan getLoanById(int id) throws SQLException {
        InputValidator.validatePositiveId(id, "Loan ID");
        String sql = BASE_SELECT + "WHERE l.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoan(rs);
                } else {
                    throw new LoanNotFoundException(id);
                }
            }
        }
    }

    /**
     * Retrieves all loans belonging to a specific borrower.
     *
     * @param borrowerId Borrower primary key
     * @return list of Loan entities for that borrower
     * @throws SQLException if database read fails
     */
    public List<Loan> getLoansByBorrowerId(int borrowerId) throws SQLException {
        InputValidator.validatePositiveId(borrowerId, "Borrower ID");
        String sql = BASE_SELECT + "WHERE l.borrower_id = ? ORDER BY l.id DESC";
        return fetchLoans(sql, borrowerId);
    }

    /**
     * Retrieves all loans with status = 'active'.
     *
     * @return list of active Loan entities
     * @throws SQLException if database read fails
     */
    public List<Loan> getAllActiveLoans() throws SQLException {
        String sql = BASE_SELECT + "WHERE l.status = 'active' ORDER BY l.id DESC";
        return fetchLoans(sql, null);
    }

    /**
     * Retrieves all loans with status = 'completed'.
     *
     * @return list of completed Loan entities
     * @throws SQLException if database read fails
     */
    public List<Loan> getAllCompletedLoans() throws SQLException {
        String sql = BASE_SELECT + "WHERE l.status = 'completed' ORDER BY l.id DESC";
        return fetchLoans(sql, null);
    }

    /**
     * Updates the status of an existing loan.
     *
     * @param loanId    Loan primary key
     * @param newStatus New status value: 'active', 'completed', or 'defaulted'
     * @throws LoanNotFoundException if loan does not exist
     * @throws ValidationException if status value is invalid
     * @throws SQLException if database update fails
     */
    public void updateLoanStatus(int loanId, String newStatus) throws SQLException {
        InputValidator.validatePositiveId(loanId, "Loan ID");
        InputValidator.validateLoanStatus(newStatus);

        // Confirm loan exists before updating
        getLoanById(loanId);

        String sql = "UPDATE loans SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.toLowerCase());
            ps.setInt(2, loanId);
            ps.executeUpdate();
        }
    }

    /**
     * Calculates total interest for a loan based on interest type, rate, period, and duration.
     *
     * - Flat / Simple: total_interest = principal * (rate/100) * time_in_rate_periods
     * - Reducing Balance: amortization formula — M = P * r * (1+r)^n / ((1+r)^n - 1)
     *   The result for reducing balance is an ESTIMATE (actual depends on payment schedule).
     *
     * @param principal      Loan principal amount
     * @param interestRate   Annual/periodic interest rate as percentage (e.g. 5.0 = 5%)
     * @param ratePeriod     Period the rate applies to: 'daily', 'weekly', 'monthly', 'annually'
     * @param interestType   'flat', 'simple', or 'reducing_balance'
     * @param durationMonths Loan duration in months
     * @return calculated total interest
     */
    public BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal interestRate,
                                              String ratePeriod, String interestType,
                                              int durationMonths) {
        // Convert percentage to decimal (e.g. 5% -> 0.05)
        BigDecimal rateDecimal = interestRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        if ("reducing_balance".equalsIgnoreCase(interestType)) {
            // Convert the given rate to an equivalent monthly rate
            BigDecimal monthlyRate = getMonthlyRate(rateDecimal, ratePeriod);

            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
                // Zero interest rate — no interest
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            // Amortization: M = P * r * (1+r)^n / ((1+r)^n - 1)
            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal power = BigDecimal.ONE;
            for (int i = 0; i < durationMonths; i++) {
                power = power.multiply(onePlusR);
            }

            BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
            BigDecimal denominator = power.subtract(BigDecimal.ONE);
            BigDecimal monthlyPayment = numerator.divide(denominator, 10, RoundingMode.HALF_UP);
            BigDecimal totalPaid = monthlyPayment.multiply(new BigDecimal(durationMonths));

            return totalPaid.subtract(principal).setScale(2, RoundingMode.HALF_UP);

        } else {
            // Flat or Simple: interest = principal * rate * time_periods
            BigDecimal timePeriods = convertMonthsToPeriods(durationMonths, ratePeriod);
            return principal.multiply(rateDecimal).multiply(timePeriods).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Converts loan duration in months to the number of rate periods.
     * Monthly -> direct; Annually -> divide by 12; Weekly -> multiply by 52/12; Daily -> multiply by 365/12.
     */
    private BigDecimal convertMonthsToPeriods(int months, String ratePeriod) {
        BigDecimal monthsBD = new BigDecimal(months);

        switch (ratePeriod.toLowerCase()) {
            case "monthly":
                return monthsBD;
            case "annually":
                // months / 12 = number of years
                return monthsBD.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            case "weekly":
                // months * (52/12) = number of weeks
                return monthsBD.multiply(new BigDecimal("52"))
                        .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            case "daily":
                // months * (365/12) = number of days
                return monthsBD.multiply(new BigDecimal("365"))
                        .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            default:
                return monthsBD;
        }
    }

    /**
     * Converts the given rate (as a decimal per its period) to an equivalent monthly decimal rate.
     * Used for reducing balance amortization, which works month-by-month.
     */
    private BigDecimal getMonthlyRate(BigDecimal rateDecimal, String ratePeriod) {
        switch (ratePeriod.toLowerCase()) {
            case "monthly":
                return rateDecimal;
            case "annually":
                // annual rate / 12 = monthly rate
                return rateDecimal.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            case "weekly":
                // weekly rate * 52 / 12 = monthly rate
                return rateDecimal.multiply(new BigDecimal("52"))
                        .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            case "daily":
                // daily rate * 365 / 12 = monthly rate
                return rateDecimal.multiply(new BigDecimal("365"))
                        .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            default:
                return rateDecimal;
        }
    }

    /**
     * Helper to run a SELECT query and return a list of Loan objects.
     * Accepts an optional Integer parameter for queries that need a WHERE ? binding.
     */
    private List<Loan> fetchLoans(String sql, Integer param) throws SQLException {
        List<Loan> loans = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (param != null) {
                ps.setInt(1, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapResultSetToLoan(rs));
                }
            }
        }

        return loans;
    }

    /**
     * Maps a current ResultSet row to a Loan entity POJO.
     */
    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
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

        Date sd = rs.getDate("start_date");
        LocalDate startDate = sd != null ? sd.toLocalDate() : null;

        Date dd = rs.getDate("due_date");
        LocalDate dueDate = dd != null ? dd.toLocalDate() : null;

        String status = rs.getString("status");
        String remarks = rs.getString("remarks");

        Timestamp cat = rs.getTimestamp("created_at");
        LocalDateTime createdAt = cat != null ? cat.toLocalDateTime() : null;

        Timestamp uat = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = uat != null ? uat.toLocalDateTime() : null;

        return new Loan(id, borrowerId, borrowerName, principal, interestType, interestRate,
                ratePeriod, durationMonths, totalInterest, totalPayable, outstandingBalance,
                startDate, dueDate, status, remarks, createdAt, updatedAt);
    }
}
