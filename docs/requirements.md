# LoanLedger — Feature Requirements

This document lists all planned features, organised by module.
Implementation is incremental — modules are built one at a time.

---

## Module 1: Borrower Management

**Purpose:** Register and manage the people who take out loans.

| Feature | Description |
|---|---|
| Register borrower | Add a new borrower with personal and contact details |
| View all borrowers | List all active borrowers |
| Search borrower | Find by name, ID, or phone number |
| Update borrower | Edit contact information |
| Deactivate borrower | Soft-delete — marks as inactive, never deletes the record |

**Borrower data:** full name, phone, address, ID number (e.g. government ID), date registered, status (active/inactive).

---

## Module 2: Loan Management

**Purpose:** Create and track individual loan records linked to borrowers.

| Feature | Description |
|---|---|
| Create loan | Record a new loan for an existing borrower |
| View loans | List all loans (filter by status: active, closed, overdue) |
| Search loan | Find by loan ID or borrower |
| View loan details | Show full loan terms, repayment schedule, payment history |
| Close loan | Mark a fully repaid loan as closed |

**Loan data:** loan ID, borrower, principal amount, interest type, interest rate, term, start date, due date, status.

---

## Module 3: Interest Calculation

**Purpose:** Calculate interest according to the agreed loan terms.

| Type | Description |
|---|---|
| Flat interest | Interest applied on original principal for the full term |
| Reducing balance | Interest applied on the outstanding principal after each payment |
| Simple interest | Principal × Rate × Time |

Different rate periods:
- Per day
- Per week
- Per month
- Per annum

**Rule:** Interest calculation type and rate must be fixed at loan creation and never changed retroactively.

---

## Module 4: Payment Management

**Purpose:** Record and track all repayments made by borrowers.

| Feature | Description |
|---|---|
| Record repayment | Accept a payment and allocate it to interest then principal |
| View payment history | List all payments for a specific loan |
| Generate transaction ID | Unique identifier for each payment record |
| Partial repayment | Accept amounts less than the scheduled instalment |
| Early loan closure | Accept full outstanding balance before the final due date |

**Rule:** Payments are never deleted or overwritten. Corrections are made via reversal entries.

---

## Module 5: Instalment Management

**Purpose:** Generate and track the repayment schedule for each loan.

| Feature | Description |
|---|---|
| Generate schedule | Create a list of instalments at loan creation |
| Instalment frequency | Daily, weekly, or monthly |
| Track upcoming | Show instalments due in the next N days |
| Track overdue | List unpaid instalments past their due date |

---

## Module 6: Reports

**Purpose:** Financial reports for the lender's reference.

| Report | Description |
|---|---|
| Borrower statement | Full history of loans and payments for one borrower |
| Loan statement | Detailed repayment history and outstanding balance for one loan |
| Collection report | Summary of payments received in a date range |
| Outstanding loans | List of all loans with unpaid balances |

---

## Module 7: Expense Management

**Purpose:** Track business operating expenses.

| Feature | Description |
|---|---|
| Record expense | Log an expense with amount, category, and date |
| View expenses | List all expenses (filter by date or category) |
| Monthly summary | Total expenses for a given month |

---

## Module 8: Authentication

**Purpose:** Secure the application so only the owner can log in.

| Feature | Description |
|---|---|
| Owner login | Username and password authentication at startup |
| Password hashing | Passwords stored using a secure hash (never plain text) |
| Session | Access granted for the duration of the application session |

---

## Module 9: Dashboard and Audit

**Purpose:** Give the lender a quick financial overview and a full activity trail.

| Feature | Description |
|---|---|
| Dashboard | Total loans outstanding, total collected today/month, overdue count |
| Activity log | Record who did what and when (login, loan creation, payment recording) |
| Audit trail | Immutable log — records are only added, never modified or deleted |
