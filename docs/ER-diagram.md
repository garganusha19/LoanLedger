# LoanLedger — Entity-Relationship Diagram

> **Status:** Placeholder — the full ER diagram will be completed after all table designs are finalised.

---

## Planned Entities and Relationships

```
owners
  │
  │ manages (1 owner → many loans)
  │
  ▼
borrowers ──────────── loans
  (1 borrower         (1 loan → many instalments)
   → many loans)      (1 loan → many payments)
                            │
                       instalments    payments
                                          │
                                   payment_allocations
                                   (links payment to
                                    instalment amounts)

owners ─── audit_log   (every action logged against an owner session)

expenses               (standalone — linked to owner, not borrower/loan)
```

---

## Relationship Summary

| Relationship | Type | Description |
|---|---|---|
| `borrowers` → `loans` | One-to-Many | One borrower may have multiple loans |
| `loans` → `instalments` | One-to-Many | One loan has a schedule of instalment entries |
| `loans` → `payments` | One-to-Many | One loan receives multiple repayment records |
| `payments` → `payment_allocations` | One-to-Many | One payment may be split across interest and principal |
| `owners` → `audit_log` | One-to-Many | Every auditable action is linked to the owner session |

---

## Full ER Diagram

> To be drawn here (using text, Mermaid, or an image) once all table columns are defined.

A visual ER diagram will be added in `docs/er-diagram.png` using MySQL Workbench's EER Diagram feature once the schema is complete.
