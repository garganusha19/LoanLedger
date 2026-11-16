package com.loanledger;

import com.loanledger.exception.ValidationException;
import com.loanledger.util.InputValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InputValidator Unit Tests")
class InputValidatorTest {

    @Test
    @DisplayName("Valid borrower data should pass validation")
    void testValidBorrower() {
        assertDoesNotThrow(() ->
                InputValidator.validateBorrower("Ananya Sen", "9876543210", "12 Park Street, Kolkata", "PAN12345")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "A", "John@Doe", "12345"})
    @DisplayName("Invalid names should throw ValidationException")
    void testInvalidFullName(String invalidName) {
        assertThrows(ValidationException.class, () -> InputValidator.validateFullName(invalidName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"John Doe", "Dr. A. P. J. Kalam", "Mary-Jane Watson", "O'Connor"})
    @DisplayName("Valid names should pass validation")
    void testValidFullName(String validName) {
        assertDoesNotThrow(() -> InputValidator.validateFullName(validName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "abcdef", "98765432101234567890", "phone#123"})
    @DisplayName("Invalid phone numbers should throw ValidationException")
    void testInvalidPhone(String invalidPhone) {
        assertThrows(ValidationException.class, () -> InputValidator.validatePhone(invalidPhone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"9876543210", "+919876543210", "98765-43210", "+1 (800) 1234567"})
    @DisplayName("Valid phone numbers should pass validation")
    void testValidPhone(String validPhone) {
        assertDoesNotThrow(() -> InputValidator.validatePhone(validPhone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "ab"})
    @DisplayName("Invalid addresses should throw ValidationException")
    void testInvalidAddress(String invalidAddress) {
        assertThrows(ValidationException.class, () -> InputValidator.validateAddress(invalidAddress));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "ab", "id with spaces", "id#special!"})
    @DisplayName("Invalid ID numbers should throw ValidationException")
    void testInvalidIdNumber(String invalidId) {
        assertThrows(ValidationException.class, () -> InputValidator.validateIdNumber(invalidId));
    }

    @Test
    @DisplayName("Positive ID validation should pass for positive ints and throw for <= 0")
    void testPositiveId() {
        assertDoesNotThrow(() -> InputValidator.validatePositiveId(1, "Test ID"));
        assertThrows(ValidationException.class, () -> InputValidator.validatePositiveId(0, "Test ID"));
        assertThrows(ValidationException.class, () -> InputValidator.validatePositiveId(-5, "Test ID"));
    }
}

