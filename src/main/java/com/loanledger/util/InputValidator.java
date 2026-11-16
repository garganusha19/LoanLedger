package com.loanledger.util;

import com.loanledger.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * InputValidator — Utility for validating user inputs and domain formats.
 *
 * Responsibilities:
 * - Checks for non-empty fields, valid phone numbers, names, and ID numbers.
 * - Throws ValidationException with informative messages on validation failure.
 */
public class InputValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.'\\-]{2,100}$");
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-]{3,50}$");

    /**
     * Validates borrower full name.
     */
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

    /**
     * Validates borrower phone number.
     */
    public static void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number is required and cannot be empty.");
        }
        String trimmed = phone.trim().replaceAll("[\\s\\-()]", "");
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Invalid phone number: '" + phone + "'. Must be 10 to 15 digits (optional leading +).");
        }
    }

    /**
     * Validates borrower residential or business address.
     */
    public static void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new ValidationException("Address is required and cannot be empty.");
        }
        String trimmed = address.trim();
        if (trimmed.length() < 3 || trimmed.length() > 255) {
            throw new ValidationException("Address must be between 3 and 255 characters.");
        }
    }

    /**
     * Validates borrower government or national identity number.
     */
    public static void validateIdNumber(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new ValidationException("Government ID number is required and cannot be empty.");
        }
        String trimmed = idNumber.trim();
        if (!ID_NUMBER_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("ID number must be 3-50 alphanumeric characters (hyphens allowed).");
        }
    }

    /**
     * Validates positive entity ID.
     */
    public static void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new ValidationException(fieldName + " must be a positive integer greater than 0.");
        }
    }

    /**
     * Validates all fields for a Borrower entity.
     */
    public static void validateBorrower(String fullName, String phone, String address, String idNumber) {
        validateFullName(fullName);
        validatePhone(phone);
        validateAddress(address);
        validateIdNumber(idNumber);
    }

    private InputValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }
}

