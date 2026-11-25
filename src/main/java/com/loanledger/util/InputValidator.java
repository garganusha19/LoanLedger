package com.loanledger.util;

import com.loanledger.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.'\\-]{2,100}$");
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-]{3,50}$");

    public static void validateFullName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Full name is required and cannot be empty.");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 100) {
            throw new ValidationException("Full name must be between 2 and 100 characters.");
        }
        if (!NAME_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Full name contains invalid characters. Use letters, spaces, dots, or hyphens.");
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number is required and cannot be empty.");
        }
        String trimmed = phone.trim().replaceAll("[\\s\\-()]", "");
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Invalid phone number: '" + phone + "'. Must be 10 to 15 digits (optional leading +).");
        }
    }

    public static void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new ValidationException("Address is required and cannot be empty.");
        }
        String trimmed = address.trim();
        if (trimmed.length() < 3 || trimmed.length() > 255) {
            throw new ValidationException("Address must be between 3 and 255 characters.");
        }
    }

    public static void validateIdNumber(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new ValidationException("Government ID number is required and cannot be empty.");
        }
        String trimmed = idNumber.trim();
        if (!ID_NUMBER_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("ID number must be 3-50 alphanumeric characters (hyphens allowed).");
        }
    }

    public static void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new ValidationException(fieldName + " must be a positive integer greater than 0.");
        }
    }

    public static void validateBorrower(String fullName, String phone, String address, String idNumber) {
        validateFullName(fullName);
        validatePhone(phone);
        validateAddress(address);
        validateIdNumber(idNumber);
    }

    public static void validatePrincipal(BigDecimal principal) {
        if (principal == null) {
            throw new ValidationException("Principal amount is required.");
        }
        if (principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Principal must be greater than zero.");
        }
    }

    public static void validateInterestRate(BigDecimal rate) {
        if (rate == null) {
            throw new ValidationException("Interest rate is required.");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Interest rate cannot be negative.");
        }
        if (rate.compareTo(new BigDecimal("1000")) > 0) {
            throw new ValidationException("Interest rate cannot exceed 1000%.");
        }
    }

    public static void validateDurationMonths(int months) {
        if (months < 1) {
            throw new ValidationException("Loan duration must be at least 1 month.");
        }
        if (months > 360) {
            throw new ValidationException("Loan duration cannot exceed 360 months (30 years).");
        }
    }

    public static void validateInterestType(String interestType) {
        if (interestType == null || interestType.trim().isEmpty()) {
            throw new ValidationException("Interest type is required.");
        }
        String t = interestType.trim().toLowerCase();
        if (!t.equals("flat") && !t.equals("simple") && !t.equals("reducing_balance")) {
            throw new ValidationException("Interest type must be 'flat', 'simple', or 'reducing_balance'.");
        }
    }

    public static void validateRatePeriod(String ratePeriod) {
        if (ratePeriod == null || ratePeriod.trim().isEmpty()) {
            throw new ValidationException("Rate period is required.");
        }
        String p = ratePeriod.trim().toLowerCase();
        if (!p.equals("daily") && !p.equals("weekly") && !p.equals("monthly") && !p.equals("annually")) {
            throw new ValidationException("Rate period must be 'daily', 'weekly', 'monthly', or 'annually'.");
        }
    }

    public static void validateLoanStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Loan status is required.");
        }
        String s = status.trim().toLowerCase();
        if (!s.equals("active") && !s.equals("completed") && !s.equals("defaulted")) {
            throw new ValidationException("Loan status must be 'active', 'completed', or 'defaulted'.");
        }
    }

    private InputValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }
}
