# LoanLedger — Database Design

> **Status:** Placeholder — table designs will be finalised incrementally before each module is implemented.

---

## Database

| Property | Value |
|---|---|
| Database name | `loanledger_db` |
| Character set | `utf8mb4` |
| Collation | `utf8mb4_unicode_ci` |
| Engine | `InnoDB` (for foreign key support and transactions) |

```sql
CREATE DATABASE IF NOT EXISTS loanledger_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE loanledger_db;
```

---

## Planned Tables

The following tables will be designed in detail before their respective modules are implemented.

| Table | Module | Purpose |
|---|---|---|
| `owners` | Authentication | Lender account and hashed password |
| `borrowers` | Borrower Management | Borrower profiles |
| `loans` | Loan Management | Loan records linked to borrowers |
| `instalments` | Instalment Management | Individual repayment schedule entries |
| `payments` | Payment Management | Repayment transactions |
| `payment_allocations` | Payment Management | How each payment is split (interest vs principal) |
| `expenses` | Expense Management | Business expense records |
| `audit_log` | Dashboard and Audit | Immutable activity trail |

---

## Design Conventions

- All tables use a surrogate primary key: `id INT AUTO_INCREMENT PRIMARY KEY`.
- All monetary columns use `DECIMAL(15, 2)` — never `FLOAT` or `DOUBLE`.
- All date/time columns use `DATETIME` or `DATE` as appropriate.
- Soft-delete pattern: use a `status` column (e.g. `ENUM('active','inactive','closed')`) instead of `DELETE`.
- All tables include `created_at DATETIME DEFAULT CURRENT_TIMESTAMP` and `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`.

---

## Table Designs

### `owners` (Module 8: Authentication)
Stores lender/owner credentials and metadata.
- `id INT AUTO_INCREMENT PRIMARY KEY`
- `username VARCHAR(50) NOT NULL UNIQUE`
- `password_hash VARCHAR(255) NOT NULL`
- `full_name VARCHAR(100) NOT NULL`
- `email VARCHAR(100) UNIQUE`
- `status ENUM('active', 'inactive') NOT NULL DEFAULT 'active'`
- `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`
- `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

### `borrowers` (Module 1: Borrower Management)
Stores borrower contact and identity profiles.
- `id INT AUTO_INCREMENT PRIMARY KEY`
- `full_name VARCHAR(100) NOT NULL`
- `phone VARCHAR(20) NOT NULL UNIQUE`
- `address VARCHAR(255) NOT NULL`
- `id_number VARCHAR(50) NOT NULL UNIQUE` (Government ID)
- `date_registered DATE NOT NULL`
- `status ENUM('active', 'inactive') NOT NULL DEFAULT 'active'`
- `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`
- `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

### `loans` (Module 2: Loan Management)
Records loans linked to borrowers with terms and calculated totals.
- `id INT AUTO_INCREMENT PRIMARY KEY`
- `borrower_id INT NOT NULL` (FK -> `borrowers.id`)
- `principal_amount DECIMAL(15, 2) NOT NULL`
- `interest_type ENUM('FLAT', 'REDUCING_BALANCE', 'SIMPLE') NOT NULL`
- `interest_rate DECIMAL(5, 2) NOT NULL`
- `rate_period ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'ANNUAL', 'ANNUALLY') NOT NULL`
- `term_months INT NOT NULL`
- `total_interest DECIMAL(15, 2) NOT NULL DEFAULT 0.00`
- `total_payable DECIMAL(15, 2) NOT NULL DEFAULT 0.00`
- `outstanding_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00`
- `start_date DATE NOT NULL`
- `due_date DATE NOT NULL`
- `status ENUM('active', 'closed', 'completed', 'overdue', 'defaulted') NOT NULL DEFAULT 'active'`
- `remarks VARCHAR(255)`
- `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`
- `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

### `instalments` (Module 5: Instalment Management)
Tracks repayment schedule dates and breakdown for each loan.
- `id INT AUTO_INCREMENT PRIMARY KEY`
- `loan_id INT NOT NULL` (FK -> `loans.id`)
- `instalment_number INT NOT NULL`
- `due_date DATE NOT NULL`
- `principal_amount DECIMAL(15, 2) NOT NULL`
- `interest_amount DECIMAL(15, 2) NOT NULL`
- `total_amount DECIMAL(15, 2) NOT NULL`
- `paid_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00`
- `status ENUM('unpaid', 'partially_paid', 'paid', 'overdue') NOT NULL DEFAULT 'unpaid'`
- `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`
- `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

### `payments` (Module 4: Payment Management)
Immutable transaction log of payments received.
- `id INT AUTO_INCREMENT PRIMARY KEY`
- `loan_id INT NOT NULL` (FK -> `loans.id`)
- `transaction_id VARCHAR(50) NOT NULL UNIQUE`
- `amount DECIMAL(15, 2) NOT NULL`
- `payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `payment_method ENUM('CASH', 'BANK_TRANSFER', 'UPI', 'CHEQUE', 'OTHER') NOT NULL DEFAULT 'CASH'`
- `notes TEXT`
- `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`
- `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

### `payment_allocations` (Module 4: Payment Management)
Splits each payment into principal, interest, and penalty portions.
- `id INT AUTO_INCREMENT PRIMARY KEY`
- `payment_id INT NOT NULL` (FK -> `payments.id`)
- `instalment_id INT NULL` (FK -> `instalments.id`)
- `principal_portion DECIMAL(15, 2) NOT NULL DEFAULT 0.00`
- `interest_portion DECIMAL(15, 2) NOT NULL DEFAULT 0.00`
- `penalty_portion DECIMAL(15, 2) NOT NULL DEFAULT 0.00`
- `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`

### `expenses` (Module 7: Expense Management)
Tracks business expenses incurred by the lender.
- `id INT AUTO_INCREMENT PRIMARY KEY`
- `owner_id INT NULL` (FK -> `owners.id`)
- `category VARCHAR(50) NOT NULL`
- `amount DECIMAL(15, 2) NOT NULL`
- `expense_date DATE NOT NULL`
- `description VARCHAR(255)`
- `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`
- `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

### `audit_log` (Module 9: Dashboard and Audit)
Immutable audit trail of actions performed.
- `id INT AUTO_INCREMENT PRIMARY KEY`
- `owner_id INT NULL` (FK -> `owners.id`)
- `action VARCHAR(50) NOT NULL`
- `entity_name VARCHAR(50) NOT NULL`
- `entity_id INT NULL`
- `details TEXT`
- `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`
