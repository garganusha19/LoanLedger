package com.loanledger.exception;

/**
 * LoanNotFoundException — Thrown when a loan record cannot be found by its ID.
 */
public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(int id) {
        super("Loan not found with ID: " + id);
    }
}
