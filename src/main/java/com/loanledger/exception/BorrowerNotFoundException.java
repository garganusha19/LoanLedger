package com.loanledger.exception;

/**
 * BorrowerNotFoundException — Thrown when a requested borrower cannot be found.
 */
public class BorrowerNotFoundException extends RuntimeException {

    public BorrowerNotFoundException(String message) {
        super(message);
    }

    public BorrowerNotFoundException(int id) {
        super("Borrower not found with ID: " + id);
    }
}

