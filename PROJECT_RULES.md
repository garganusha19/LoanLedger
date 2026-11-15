# LoanLedger — Project Rules

This document is the authoritative reference for architecture decisions and coding standards.
All contributors must follow these rules consistently.

---

## Architecture: Layered Design

```
┌─────────────────────────────────┐
│           UI Layer              │  Console menus, user input/output
├─────────────────────────────────┤
│         Service Layer           │  Business logic + JDBC database operations
├─────────────────────────────────┤
│          Model Layer            │  Entity classes (POJOs)
├─────────────────────────────────┤
│          Util Layer             │  DBConnection, input validation
├─────────────────────────────────┤
│        Exception Layer          │  Custom exceptions
└─────────────────────────────────┘
```

---

## Layer Responsibilities

### Model (`com.loanledger.model`)
- Represents a database entity as a Java class.
- Contains: fields, constructor(s), getters, setters.
- Contains NO business logic, NO SQL, NO console I/O.
- Example: `Borrower.java`, `Loan.java`, `Payment.java`

### Service (`com.loanledger.service`)
- Contains all business logic.
- Performs ALL database operations using JDBC directly (no DAO/Repository).
- Uses `DBConnection` to get a `Connection`.
- Uses `PreparedStatement` and try-with-resources for every JDBC operation.
- Uses `BigDecimal` for all monetary calculations.
- Example: `BorrowerService.java`, `LoanService.java`

### UI (`com.loanledger.ui`)
- Handles console menus and user input.
- Calls Service methods and displays their results.
- Contains NO SQL queries.
- Contains NO business logic or calculations.
- Example: `BorrowerMenu.java`, `MainMenu.java`

### Util (`com.loanledger.util`)
- `DBConnection.java` — reads `application.properties` and returns a JDBC `Connection`.
- Future: `InputValidator.java` — validates user input (non-empty, numeric ranges, date formats).

### Exception (`com.loanledger.exception`)
- Custom exceptions that make error messages meaningful.
- Example: `LoanNotFoundException`, `InvalidPaymentException`

---

## Coding Rules

### Database Access
- **NEVER** use DAO or Repository classes. Services access the DB directly.
- Always use `PreparedStatement` — never concatenate SQL strings with user input.
- Always use try-with-resources for `Connection`, `PreparedStatement`, and `ResultSet`.
- Use database transactions (`connection.setAutoCommit(false)`) for operations that touch multiple tables.

```java
// CORRECT
try (Connection conn = DBConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement("SELECT * FROM borrowers WHERE id = ?")) {
    ps.setInt(1, id);
    try (ResultSet rs = ps.executeQuery()) {
        // process results
    }
}

// WRONG — never do this
Statement st = conn.createStatement();
ResultSet rs = st.executeQuery("SELECT * FROM borrowers WHERE id = " + id);
```

### Monetary Values
- **Always use `BigDecimal`** for money. Never use `double` or `float`.
- Use `BigDecimal.valueOf(string)` or `new BigDecimal("100.00")` — never `new BigDecimal(100.0)`.
- MySQL columns for money: `DECIMAL(15, 2)`.

```java
// CORRECT
BigDecimal principal = new BigDecimal("50000.00");

// WRONG
double principal = 50000.00;
```

### Exception Handling
- Do not silently swallow exceptions with an empty `catch` block.
- Log or rethrow meaningful messages.
- Use custom exceptions for domain errors.

### Data Integrity
- Never permanently delete financial records. Use a `status` column or soft-delete.
- Use reversals or adjustments to correct payments — never overwrite payment records.
- Keep principal and interest amounts separately stored and identifiable.

---

## Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Class | PascalCase | `BorrowerService` |
| Method | camelCase | `registerBorrower()` |
| Variable | camelCase | `loanAmount` |
| Constant | UPPER_SNAKE_CASE | `MAX_LOAN_TERM` |
| Package | lowercase | `com.loanledger.service` |
| DB table | snake_case | `borrowers`, `loan_payments` |
| DB column | snake_case | `first_name`, `created_at` |

---

## What We Do NOT Use

| Avoided Pattern | Reason |
|---|---|
| DAO / Repository classes | Unnecessary abstraction for this project size |
| ORM (Hibernate, JPA) | Keeps JDBC learning clear and transparent |
| `double` / `float` for money | Floating-point precision errors |
| Raw `Statement` with string concatenation | SQL injection risk |
| `System.exit()` inside service or model | UI layer controls application lifecycle |
