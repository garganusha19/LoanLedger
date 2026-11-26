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

public class BorrowerService {

    public Borrower registerBorrower(Borrower borrower) throws SQLException {
        InputValidator.validateName(borrower.getFullName());
        InputValidator.validatePhone(borrower.getPhone());
        InputValidator.validateAddress(borrower.getAddress());
        InputValidator.validateIdNumber(borrower.getIdNumber());

        // Check if phone already exists
        String checkPhone = "SELECT COUNT(*) FROM borrowers WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkPhone)) {
            ps.setString(1, borrower.getPhone().trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new IllegalArgumentException("A borrower with this phone number already exists.");
            }
        }

        // Check if government ID already exists
        String checkId = "SELECT COUNT(*) FROM borrowers WHERE id_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkId)) {
            ps.setString(1, borrower.getIdNumber().trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new IllegalArgumentException("A borrower with this government ID already exists.");
            }
        }

        String sql = "INSERT INTO borrowers (full_name, phone, address, id_number, date_registered, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, borrower.getFullName().trim());
            ps.setString(2, borrower.getPhone().trim());
            ps.setString(3, borrower.getAddress().trim());
            ps.setString(4, borrower.getIdNumber().trim());
            ps.setDate(5, Date.valueOf(borrower.getDateRegistered()));
            ps.setString(6, "active");

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                borrower.setId(keys.getInt(1));
            }
        }

        return borrower;
    }

    public List<Borrower> getAllActiveBorrowers() throws SQLException {
        List<Borrower> list = new ArrayList<>();
        String sql = "SELECT id, full_name, phone, address, id_number, date_registered, " +
                     "status, created_at, updated_at FROM borrowers WHERE status = 'active' ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(readBorrower(rs));
            }
        }
        return list;
    }

    public Borrower getBorrowerById(int id) throws SQLException {
        String sql = "SELECT id, full_name, phone, address, id_number, date_registered, " +
                     "status, created_at, updated_at FROM borrowers WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return readBorrower(rs);
            } else {
                throw new IllegalArgumentException("Borrower not found with ID: " + id);
            }
        }
    }

    public List<Borrower> searchBorrowers(String query) throws SQLException {
        List<Borrower> list = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return list;
        }

        String trimmed = query.trim();

        int numericId = -1;
        try {
            numericId = Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            // query is not a number
        }

        String wildcard = "%" + trimmed + "%";
        String sql = "SELECT id, full_name, phone, address, id_number, date_registered, " +
                     "status, created_at, updated_at FROM borrowers " +
                     "WHERE id = ? OR full_name LIKE ? OR phone = ? OR id_number = ? ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, numericId);
            ps.setString(2, wildcard);
            ps.setString(3, trimmed);
            ps.setString(4, trimmed);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(readBorrower(rs));
            }
        }
        return list;
    }

    public void updateBorrower(Borrower borrower) throws SQLException {
        getBorrowerById(borrower.getId());

        // Check phone is not taken by someone else
        String checkPhone = "SELECT COUNT(*) FROM borrowers WHERE phone = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkPhone)) {
            ps.setString(1, borrower.getPhone().trim());
            ps.setInt(2, borrower.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new IllegalArgumentException("This phone number is already used by another borrower.");
            }
        }

        // Check ID is not taken by someone else
        String checkId = "SELECT COUNT(*) FROM borrowers WHERE id_number = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkId)) {
            ps.setString(1, borrower.getIdNumber().trim());
            ps.setInt(2, borrower.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new IllegalArgumentException("This government ID is already used by another borrower.");
            }
        }

        String sql = "UPDATE borrowers SET full_name = ?, phone = ?, address = ?, id_number = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, borrower.getFullName().trim());
            ps.setString(2, borrower.getPhone().trim());
            ps.setString(3, borrower.getAddress().trim());
            ps.setString(4, borrower.getIdNumber().trim());
            ps.setInt(5, borrower.getId());

            ps.executeUpdate();
        }
    }

    public void deactivateBorrower(int id) throws SQLException {
        getBorrowerById(id);

        String sql = "UPDATE borrowers SET status = 'inactive' WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Borrower readBorrower(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String fullName = rs.getString("full_name");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        String idNumber = rs.getString("id_number");
        String status = rs.getString("status");

        LocalDate dateRegistered = null;
        if (rs.getDate("date_registered") != null) {
            dateRegistered = rs.getDate("date_registered").toLocalDate();
        }

        LocalDateTime createdAt = null;
        if (rs.getTimestamp("created_at") != null) {
            createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        }

        LocalDateTime updatedAt = null;
        if (rs.getTimestamp("updated_at") != null) {
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        }

        return new Borrower(id, fullName, phone, address, idNumber, dateRegistered, status, createdAt, updatedAt);
    }
}
