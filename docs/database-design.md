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

## Table Designs (populated incrementally)

### `owners` — *(to be designed with Module 8: Authentication)*

### `borrowers` — *(to be designed with Module 1: Borrower Management)*

### `loans` — *(to be designed with Module 2: Loan Management)*

### `instalments` — *(to be designed with Module 5: Instalment Management)*

### `payments` — *(to be designed with Module 4: Payment Management)*

### `payment_allocations` — *(to be designed with Module 4: Payment Management)*

### `expenses` — *(to be designed with Module 7: Expense Management)*

### `audit_log` — *(to be designed with Module 9: Dashboard and Audit)*
