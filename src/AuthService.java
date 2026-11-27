import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public static final int LOGIN_SUCCESS = 1;
    public static final int USER_NOT_FOUND = 2;
    public static final int INVALID_PASSWORD = 3;

    public int login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return -1;
        }

        String sql = "SELECT * FROM lenders WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword != null && storedPassword.equals(password)) {
                        return LOGIN_SUCCESS;
                    } else {
                        return INVALID_PASSWORD;
                    }
                } else {
                    return USER_NOT_FOUND;
                }
            }
        }
    }

    public Lender getLenderByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, username, password FROM lenders WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Lender(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                }
            }
        }
        return null;
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT id FROM lenders WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean register(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        if (isUsernameTaken(username)) {
            return false;
        }

        String sql = "INSERT INTO lenders (username, password) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
