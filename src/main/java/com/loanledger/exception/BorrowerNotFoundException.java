package com.loanledger.exception;

public class BorrowerNotFoundException extends RuntimeException {

    public BorrowerNotFoundException(String message) {
        super(message);
    }

    public BorrowerNotFoundException(int id) {
        super("Borrower not found with ID: " + id);
    }
}
