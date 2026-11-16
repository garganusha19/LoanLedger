package com.loanledger.exception;

/**
 * ValidationException — Thrown when user input fails domain validation rules.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}

