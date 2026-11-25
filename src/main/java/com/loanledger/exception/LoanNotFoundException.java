package com.loanledger.exception;

public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(int id) {
        super("Loan not found with ID: " + id);
    }
}
