# LoanLedger — Financial Rules

This document defines the rules for interest calculations, payment allocation, and data integrity.
These rules must be agreed upon and documented **before** implementing the corresponding code.

---

## 1. Monetary Precision

| Rule | Detail |
|---|---|
| Java type | Always `BigDecimal` — never `double` or `float` |
| Rounding mode | `RoundingMode.HALF_UP` (standard rounding) |
| Decimal places | 2 for display and storage |
| MySQL column type | `DECIMAL(15, 2)` |

```java
// Correct
BigDecimal amount = new BigDecimal("5000.00");
BigDecimal result = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);

// WRONG — floating-point imprecision
double amount = 5000.00;
```

---

## 2. Interest Calculation Types

### 2a. Flat Interest
- Interest is calculated on the **original principal** for the **full term**, regardless of payments made.
- Total interest = Principal × Rate × Term
- Monthly instalment = (Principal + Total Interest) / Number of instalments

**Example:**
- Principal: ₱50,000
- Rate: 5% per month
- Term: 6 months
- Total interest = 50,000 × 0.05 × 6 = ₱15,000
- Total repayable = ₱65,000
- Monthly instalment = 65,000 / 6 = ₱10,833.33

### 2b. Reducing Balance Interest
- Interest is calculated on the **outstanding principal** after each payment.
- Each instalment has a different interest component (higher at start, lower at end).
- Requires an amortisation schedule.

### 2c. Simple Interest
- Interest = Principal × Rate × Time
- Rate period must be consistent with Time unit (daily rate × days, monthly rate × months, etc.)

---

## 3. Interest Rate Periods

| Period | Stored as |
|---|---|
| Per day | Daily rate (e.g. 0.001 = 0.1% per day) |
| Per week | Weekly rate |
| Per month | Monthly rate |
| Per annum | Annual rate |

The rate period is fixed at loan creation and stored with the loan record.

---

## 4. Payment Allocation Order

When a payment is received, it is allocated in this order:

1. **Penalties / late fees** (if applicable and agreed)
2. **Accrued interest**
3. **Principal**

> **Rule:** This order is the most common convention for small lending. Deviations must be explicitly agreed and documented before implementation.

---

## 5. Loan Terms Preservation

- Original loan terms (principal, rate, type, term) are **fixed at creation**.
- Terms may never be retroactively changed.
- Loan restructuring (if needed) creates a new loan record — it does not overwrite the original.

---

## 6. Payment Integrity

| Rule | Detail |
|---|---|
| No deletions | Payments are never deleted from the database |
| Corrections | Done via reversal entries — a negative payment record |
| Partial payments | Allowed — allocated to interest first, then principal |
| Early closure | Full outstanding balance may be paid before the final due date |

---

## 7. Penalties and Late Fees

> **Rule:** Penalties and compound interest will **not** be automatically applied.
> They are only recorded if the lender explicitly enters them.
> The logic and rules for penalties must be fully defined before implementation.

---

## 8. Principal and Interest Separation

- Every instalment must separately identify:
  - `principal_amount` — the portion reducing the loan balance
  - `interest_amount` — the cost of borrowing for that period
- Every payment allocation must separately record how much went to interest vs principal.

---

## 9. Calculations to Be Defined Before Implementation

The following must be explicitly designed before coding begins:

- [ ] Exact formula for each interest type with worked examples
- [ ] How to handle the last instalment rounding difference
- [ ] Penalty calculation rules (if any)
- [ ] Early closure discount rules (if any)
- [ ] Handling of payments that do not match the expected instalment amount
