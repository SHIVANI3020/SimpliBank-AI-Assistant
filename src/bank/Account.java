package bank;

import java.sql.SQLException;
import java.util.List;

public class Account {
    private final String accountHolder;
    private final String accountNumber;
    private double balance;
    private final AccountDAO dao = new AccountDAO();

    public Account(String accountHolder, String accountNumber, double initialBalance) {
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactionHistory() throws SQLException {
        return dao.getHistory(accountNumber);
    }

    public void deposit(double amount, String category) throws SQLException {
    if (amount <= 0) {
        throw new IllegalArgumentException("Deposit amount must be positive.");
    }
    balance += amount;
    dao.updateBalance(accountNumber, balance);
    dao.logTransaction(accountNumber, "DEPOSIT", amount, balance, category);
}
    public void withdraw(double amount, String category) throws InsufficientFundsException, SQLException {
    if (amount <= 0) {
        throw new IllegalArgumentException("Withdrawal amount must be positive.");
    }
    if (amount > balance) {
        throw new InsufficientFundsException(
                "Insufficient funds. Available balance: Rs." + balance);
    }
    balance -= amount;
    dao.updateBalance(accountNumber, balance);
    dao.logTransaction(accountNumber, "WITHDRAW", amount, balance, category);
}
    public void persistNewAccount(String pin) throws SQLException {
    String pinHash = PasswordUtil.hash(pin);
    dao.createAccount(accountNumber, accountHolder, balance, pinHash);
}
    public String checkForUnusualSpending(double newAmount) throws SQLException {
    List<Transaction> history = getTransactionHistory();

    double total = 0;
    int count = 0;
    for (Transaction t : history) {
        if (t.getType().equals("WITHDRAW")) {
            total += t.getAmount();
            count++;
        }
    }

    if (count == 0) {
        return null; // No past withdrawals to compare against yet
    }

    double average = total / count;

    if (newAmount > average * 3) {
        return String.format(
            "This withdrawal of Rs.%.2f is unusually high compared to your average withdrawal of Rs.%.2f.",
            newAmount, average
        );
    }

    return null; // Nothing unusual
}
public static Account login(String accountNumber, String enteredPin) throws SQLException {
    AccountDAO dao = new AccountDAO();
    String storedHash = dao.getPinHash(accountNumber);

    if (storedHash == null) {
        throw new IllegalArgumentException("No account found with number: " + accountNumber);
    }

    if (!PasswordUtil.verify(enteredPin, storedHash)) {
        throw new IllegalArgumentException("Incorrect PIN.");
    }

    String holderName = dao.getAccountHolder(accountNumber);
    double currentBalance = dao.getBalance(accountNumber);

    return new Account(holderName, accountNumber, currentBalance);
}
public void transferTo(String recipientAccountNumber, double amount) throws InsufficientFundsException, SQLException {
    if (amount <= 0) {
        throw new IllegalArgumentException("Transfer amount must be positive.");
    }
    if (recipientAccountNumber.equals(this.accountNumber)) {
        throw new IllegalArgumentException("Cannot transfer to your own account.");
    }
    if (!dao.accountExists(recipientAccountNumber)) {
        throw new IllegalArgumentException("Recipient account not found: " + recipientAccountNumber);
    }
    if (amount > balance) {
        throw new InsufficientFundsException("Insufficient funds. Available balance: Rs." + balance);
    }

    dao.transferFunds(accountNumber, recipientAccountNumber, amount);
    balance -= amount; // Keep our in-memory copy in sync with what the DB now holds
}
    
}
