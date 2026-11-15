# LoanLedger

A console-based loan management system designed for small lending businesses and local lenders who currently maintain borrower and loan records manually.

## Overview

LoanLedger helps a lender manage:

- Borrowers and their profiles
- Loans and loan terms
- Interest calculations (flat, reducing balance, simple interest)
- Repayments and payment history
- Instalments and due dates
- Financial reports and statements
- Business expenses
- Owner authentication
- Audit records and activity logs

## Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Core language |
| Maven | 3.9.9 (via Wrapper) | Build tool and dependency management |
| MySQL | 8 | Relational database |
| MySQL Connector/J | 8.4.0 | JDBC driver for MySQL |
| JUnit Jupiter | 5.11.3 | Unit testing |

## Project Structure

```
LoanLedger/
├── pom.xml                     Maven project descriptor
├── mvnw / mvnw.cmd             Maven Wrapper scripts
├── docs/                       Project documentation
└── src/
    ├── main/java/com/loanledger/
    │   ├── Main.java           Application entry point
    │   ├── model/              Entity classes
    │   ├── service/            Business logic + JDBC operations
    │   ├── ui/                 Console menus and user input
    │   ├── util/               DBConnection and input utilities
    │   └── exception/          Custom exceptions
    └── test/                   Unit tests
```

## Getting Started

### Prerequisites

- Java 21 JDK installed
- MySQL 8 running locally
- MySQL Workbench (recommended for database management)
- No global Maven installation required (Maven Wrapper is included)

### Database Setup

1. Open MySQL Workbench (or MySQL CLI).
2. Run: `CREATE DATABASE loanledger_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. Copy `src/main/resources/application.properties.template` to `application.properties`.
4. Fill in your MySQL username and password.

### Build and Run

```powershell
# Check Maven Wrapper is working
.\mvnw.cmd -version

# Compile the project
.\mvnw.cmd clean compile

# Run the application
.\mvnw.cmd exec:java "-Dexec.mainClass=com.loanledger.Main"

# Run tests
.\mvnw.cmd test
```

## Architecture

See [PROJECT_RULES.md](PROJECT_RULES.md) for the full architecture and coding rules.

## Documentation

| Document | Description |
|---|---|
| [docs/requirements.md](docs/requirements.md) | Feature requirements for all modules |
| [docs/database-design.md](docs/database-design.md) | Database table designs |
| [docs/financial-rules.md](docs/financial-rules.md) | Interest and payment calculation rules |
| [docs/ER-diagram.md](docs/ER-diagram.md) | Entity-relationship diagram |

## Development Status

- [x] Project foundation and structure
- [ ] Borrower management
- [ ] Loan management
- [ ] Interest calculations
- [ ] Payment management
- [ ] Instalment management
- [ ] Reports
- [ ] Expense management
- [ ] Authentication
- [ ] Dashboard and audit
