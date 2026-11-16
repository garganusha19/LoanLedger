package com.loanledger.service;

import com.loanledger.exception.BorrowerNotFoundException;
import com.loanledger.exception.ValidationException;
import com.loanledger.model.Borrower;
import com.loanledger.util.DBConnection;
import com.loanledger.util.InputValidator;

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
 * BorrowerService — Service Layer for Borrower Management
 *
 * Responsibilities:
 * - Implements business logic and data validation for borrowers.
 * - Executes direct JDBC operations using PreparedStatement and DBConnection.
 * - Adheres strictly to the architectural rule: NO DAO layer.
 * - Handles soft-deletes (status = 'inactive') to preserve financial history.
 */
public class BorrowerService {

    /**
     * Registers a new borrower into the system.
     *
     * @param borrower Borrower model containing full name, phone, address, and ID number
     * @return the saved Borrower with its auto-generated database ID
     * @throws ValidationException if input validation fails or duplicates exist
     * @throws SQLException if a database error occurs
     */
    public Borrower registerBorrower(Borrower borrower) throws SQLException {
        if (borrower == null) {
            throw new ValidationException("Borrower details cannot be null.");
        }

        // Validate business and format rules
        InputValidator.validateBorrower(
                borrower.getFullName(),
                borrower.getPhone(),
                borrower.getAddress(),
                borrower.getIdNumber()
        );

        // Check for duplicates
        if (isPhoneExists(borrower.getPhone(), 0)) {
            throw new ValidationException("A borrower with phone number '" + borrower.getPhone() + "' already exists.");
        }
        if (isIdNumberExists(borrower.getIdNumber(), 0)) {
            throw new ValidationException("A borrower with government ID '" + borrower.getIdNumber() + "' already exists.");
        }

        String sql = "INSERT INTO borrowers (full_name, phone, address, id_number, date_registered, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDate regDate = borrower.getDateRegistered() != null ? borrower.getDateRegistered() : LocalDate.now();
            String status = borrower.getStatus() != null ? borrower.getStatus().toLowerCase() : "active";

            ps.setString(1, borrower.getFullName().trim());
            ps.setString(2, borrower.getPhone().trim());
            ps.setString(3, borrower.getAddress().trim());
            ps.setString(4, borrower.getIdNumber().trim());
            ps.setDate(5, Date.valueOf(regDate));
            ps.setString(6, status);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating borrower failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    borrower.setId(generatedKeys.getInt(1));
                    borrower.setDateRegistered(regDate);
                    borrower.setStatus(status);
                } else {
                    throw new SQLException("Creating borrower failed, no ID obtained.");
                }
            }
        }

        return borrower;
    }

    /**
     * Retrieves all active borrowers.
     *
     * @return list of active Borrower entities
     * @throws SQLException if database read fails
     */
    public List<Borrower> getAllActiveBorrowers() throws SQLException {
        String sql = "SELECT id, full_name, phone, address, id_number, date_registered, status, created_at, updated_at " +
                     "FROM borrowers WHERE status = 'active' ORDER BY id ASC";

        List<Borrower> borrowers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                borrowers.add(mapResultSetToBorrower(rs));
            }
        }

        return borrowers;
    }

    /**
     * Retrieves all borrowers including inactive ones.
     *
     * @return list of all Borrower entities
     * @throws SQLException if database read fails
     */
    public List<Borrower> getAllBorrowers() throws SQLException {
        String sql = "SELECT id, full_name, phone, address, id_number, date_registered, status, created_at, updated_at " +
                     "FROM borrowers ORDER BY id ASC";

        List<Borrower> borrowers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                borrowers.add(mapResultSetToBorrower(rs));
            }
        }

        return borrowers;
    }

    /**
     * Finds a borrower by their primary ID.
     *
     * @param id Borrower primary key ID
     * @return Borrower model
     * @throws BorrowerNotFoundException if no borrower matches the ID
     * @throws SQLException if database read fails
     */
    public Borrower getBorrowerById(int id) throws SQLException {
        InputValidator.validatePositiveId(id, "Borrower ID");

        String sql = "SELECT id, full_name, phone, address, id_number, date_registered, status, created_at, updated_at " +
                     "FROM borrowers WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBorrower(rs);
                } else {
                    throw new BorrowerNotFoundException(id);
                }
            }
        }
    }

    /**
     * Searches for borrowers matching a query string across name, ID, or phone number.
     *
     * @param query Search query string
     * @return list of matching borrowers
     * @throws SQLException if database read fails
     */
    public List<Borrower> searchBorrowers(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveBorrowers();
        }

        String trimmed = query.trim();
        Integer numericId = null;
        try {
            numericId = Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
            // Not a purely numeric ID
        }

        String sql = "SELECT id, full_name, phone, address, id_number, date_registered, status, created_at, updated_at " +
                     "FROM borrowers WHERE id = ? OR full_name LIKE ? OR phone LIKE ? OR id_number LIKE ? " +
                     "ORDER BY id ASC";

        List<Borrower> results = new ArrayList<>();
        String wildcard = "%" + trimmed + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, numericId != null ? numericId : -1);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setString(4, wildcard);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToBorrower(rs));
                }
            }
        }

        return results;
    }

    /**
     * Updates contact and personal details for an existing borrower.
     *
     * @param borrower Borrower with updated details
     * @return true if updated successfully
     * @throws BorrowerNotFoundException if borrower does not exist
     * @throws ValidationException if validation or duplicate checks fail
     * @throws SQLException if database update fails
     */
    public boolean updateBorrower(Borrower borrower) throws SQLException {
        if (borrower == null) {
            throw new ValidationException("Borrower cannot be null.");
        }
        InputValidator.validatePositiveId(borrower.getId(), "Borrower ID");
        InputValidator.validateBorrower(
                borrower.getFullName(),
                borrower.getPhone(),
                borrower.getAddress(),
                borrower.getIdNumber()
        );

        // Ensure borrower exists
        getBorrowerById(borrower.getId());

        // Check for duplicates excluding self
        if (isPhoneExists(borrower.getPhone(), borrower.getId())) {
            throw new ValidationException("Phone number '" + borrower.getPhone() + "' is already used by another borrower.");
        }
        if (isIdNumberExists(borrower.getIdNumber(), borrower.getId())) {
            throw new ValidationException("Government ID '" + borrower.getIdNumber() + "' is already used by another borrower.");
        }

        String sql = "UPDATE borrowers SET full_name = ?, phone = ?, address = ?, id_number = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, borrower.getFullName().trim());
            ps.setString(2, borrower.getPhone().trim());
            ps.setString(3, borrower.getAddress().trim());
            ps.setString(4, borrower.getIdNumber().trim());
            ps.setInt(5, borrower.getId());

            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    /**
     * Deactivates a borrower (soft-delete).
     * The record is retained in the database with status = 'inactive' for historical integrity.
     *
     * @param id Borrower ID
     * @return true if deactivated successfully
     * @throws BorrowerNotFoundException if borrower does not exist
     * @throws SQLException if database update fails
     */
    public boolean deactivateBorrower(int id) throws SQLException {
        InputValidator.validatePositiveId(id, "Borrower ID");

        // Ensure borrower exists
        getBorrowerById(id);

        String sql = "UPDATE borrowers SET status = 'inactive' WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a phone number already exists in the database.
     */
    public boolean isPhoneExists(String phone, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrowers WHERE phone = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone.trim());
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks if a government ID number already exists in the database.
     */
    public boolean isIdNumberExists(String idNumber, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrowers WHERE id_number = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idNumber.trim());
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Maps a current ResultSet row to a Borrower entity POJO.
     */
    private Borrower mapResultSetToBorrower(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String fullName = rs.getString("full_name");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        String idNumber = rs.getString("id_number");

        Date regDate = rs.getDate("date_registered");
        LocalDate dateRegistered = regDate != null ? regDate.toLocalDate() : null;

        String status = rs.getString("status");

        Timestamp cat = rs.getTimestamp("created_at");
        LocalDateTime createdAt = cat != null ? cat.toLocalDateTime() : null;

        Timestamp uat = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = uat != null ? uat.toLocalDateTime() : null;

        return new Borrower(id, fullName, phone, address, idNumber, dateRegistered, status, createdAt, updatedAt);
    }
}

