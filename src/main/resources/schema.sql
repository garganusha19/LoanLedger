-- =============================================================================
-- LoanLedger Database Schema
-- Database: loanledger_db
-- Storage Engine: InnoDB
-- Character Set: utf8mb4 / utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS loanledger_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE loanledger_db;

-- -----------------------------------------------------------------------------
-- 1. Table: owners (Authentication & Access)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS owners (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    status ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. Table: borrowers (Borrower Management)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS borrowers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    id_number VARCHAR(50) NOT NULL UNIQUE,
    date_registered DATE NOT NULL,
    status ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. Table: loans (Loan Management)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS loans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    borrower_id INT NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    interest_type ENUM('FLAT', 'REDUCING_BALANCE', 'SIMPLE') NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    rate_period ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'ANNUAL') NOT NULL,
    term_months INT NOT NULL,
    total_interest DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_payable DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    outstanding_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    start_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status ENUM('active', 'closed', 'overdue') NOT NULL DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_loans_borrower FOREIGN KEY (borrower_id)
        REFERENCES borrowers(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. Table: instalments (Repayment Schedule)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS instalments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    instalment_number INT NOT NULL,
    due_date DATE NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    interest_amount DECIMAL(15, 2) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    paid_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status ENUM('unpaid', 'partially_paid', 'paid', 'overdue') NOT NULL DEFAULT 'unpaid',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_instalments_loan FOREIGN KEY (loan_id)
        REFERENCES loans(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uk_loan_instalment UNIQUE (loan_id, instalment_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. Table: payments (Payment Records)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(15, 2) NOT NULL,
    payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('CASH', 'BANK_TRANSFER', 'UPI', 'CHEQUE', 'OTHER') NOT NULL DEFAULT 'CASH',
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_loan FOREIGN KEY (loan_id)
        REFERENCES loans(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. Table: payment_allocations (Principal vs Interest breakdown)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_allocations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    payment_id INT NOT NULL,
    instalment_id INT NULL,
    principal_portion DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    interest_portion DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    penalty_portion DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alloc_payment FOREIGN KEY (payment_id)
        REFERENCES payments(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_alloc_instalment FOREIGN KEY (instalment_id)
        REFERENCES instalments(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. Table: expenses (Operating Expense Tracking)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    owner_id INT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenses_owner FOREIGN KEY (owner_id)
        REFERENCES owners(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. Table: audit_log (Immutable Audit Trail)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    owner_id INT NULL,
    action VARCHAR(50) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id INT NULL,
    details TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_owner FOREIGN KEY (owner_id)
        REFERENCES owners(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

