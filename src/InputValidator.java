import java.math.BigDecimal;

public class InputValidator {

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new IllegalArgumentException("Full name must be between 2 and 100 characters.");
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        String digits = phone.trim().replaceAll("[\\s\\-()]", "");
        if (!digits.matches("^\\+?[0-9]{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone number. Must be 10-15 digits.");
        }
    }

    public static void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty.");
        }
        if (address.trim().length() < 3) {
            throw new IllegalArgumentException("Address is too short.");
        }
    }

    public static void validateIdNumber(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Government ID cannot be empty.");
        }
        if (!idNumber.trim().matches("^[a-zA-Z0-9\\-]{3,50}$")) {
            throw new IllegalArgumentException("ID must be 3-50 letters, digits or hyphens.");
        }
    }

    public static void validatePrincipal(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be greater than zero.");
        }
    }

    public static void validateInterestRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        }
    }

    public static void validateDurationMonths(int months) {
        if (months < 1 || months > 360) {
            throw new IllegalArgumentException("Duration must be between 1 and 360 months.");
        }
    }
}
