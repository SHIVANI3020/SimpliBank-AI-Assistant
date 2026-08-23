# SimpliBank — AI-Powered Banking Assistant

A Java console banking application with MySQL persistence, secure PIN-based login, fund transfers, and an AI-powered natural language query engine using Google's Gemini API.

## Features

- **Core banking**: deposit, withdraw, transaction history, multi-account support
- **Secure login**: SHA-256 hashed PINs, no plain-text credentials stored
- **Atomic fund transfers**: uses database transactions (commit/rollback) to guarantee money is never lost or duplicated mid-transfer
- **AI natural language queries**: ask questions like *"how much did I spend last month?"* — Gemini generates real SQL, which is validated (SELECT-only) before running against the database
- **AI-generated answers**: raw query results are converted into natural, friendly responses
- **Unusual spending detection**: flags withdrawals significantly above your average
- **AI budgeting suggestions**: analyzes category-wise spending and suggests a savings goal

## Tech Stack

- **Language**: Java 24
- **Database**: MySQL (JDBC, DAO pattern, PreparedStatements)
- **AI**: Google Gemini API (via raw HTTP calls + org.json)
- **Testing**: JUnit 5 (12 automated tests covering deposits, withdrawals, login, and transfers)
- **Security**: SHA-256 PIN hashing, environment-variable secrets (no hardcoded credentials), SQL injection protection via parameterized queries

## Architecture

- Main.java → Console UI, menu handling
- Account.java → Core business logic (deposit, withdraw, transfer, login)
- AccountDAO.java → All raw SQL lives here (DAO pattern)
- DatabaseConnection.java → Centralized JDBC connection handling
- Transaction.java → Immutable transaction record
- PasswordUtil.java → SHA-256 PIN hashing/verification
- GeminiService.java → Raw HTTP calls to Gemini API
- NLQueryEngine.java → Prompt engineering: schema-grounded SQL generation + safety validation


## Security Notes

- Database password and Gemini API key are read from environment variables (`DB_PASSWORD`, `GEMINI_API_KEY`) — never hardcoded
- All AI-generated SQL is validated to be `SELECT`-only before execution, preventing an AI hallucination from ever modifying data
- All user-supplied values use `PreparedStatement` parameterization, preventing SQL injection
- PINs are hashed (SHA-256) before storage — never stored or compared in plain text
- Fund transfers use database transactions (`commit`/`rollback`) to guarantee atomicity

## Setup

1. Run `schema.sql` in MySQL to create the database and tables
2. Set environment variables: `DB_PASSWORD` and `GEMINI_API_KEY`
3. Compile: `javac -d out -cp "lib/*" src/bank/*.java` (Windows: use `;` between jar paths instead of `:`)
4. Run: `java -cp "out;lib/*" bank.Main`

## Testing

12 JUnit tests covering account operations, login, and transfers:

\`\`\`
java -jar lib/junit-platform-console-standalone-6.1.3.jar execute --classpath "out;lib/mysql-connector-j-26.7.0.jar;lib/json-20260814.jar" --select-package bank --details tree
\`\`\`