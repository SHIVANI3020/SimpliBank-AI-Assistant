package bank;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public void createAccount(String accountNumber, String holderName, double balance, String pinHash) throws SQLException {
    String sql = "INSERT INTO accounts (account_number, account_holder, balance, pin_hash) VALUES (?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, accountNumber);
        ps.setString(2, holderName);
        ps.setDouble(3, balance);
        ps.setString(4, pinHash);
        ps.executeUpdate();
    }
}

    public double getBalance(String accountNumber) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
                throw new SQLException("Account not found: " + accountNumber);
            }
        }
    }

    public void updateBalance(String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        }
    }

    public void logTransaction(String accountNumber, String type, double amount, double balanceAfter, String category) throws SQLException {
    String sql = "INSERT INTO transactions (account_number, type, amount, balance_after, txn_time, category) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, accountNumber);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.setDouble(4, balanceAfter);
        ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(6, category);
        ps.executeUpdate();
    }
}

    public List<Transaction> getHistory(String accountNumber) throws SQLException {
    List<Transaction> history = new ArrayList<>();
    String sql = "SELECT type, amount, balance_after, category FROM transactions WHERE account_number = ? ORDER BY txn_time";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, accountNumber);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                history.add(new Transaction(
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getDouble("balance_after"),
                        rs.getString("category")
                ));
            }
        }
    }
    return history;
}
    public int getNextAccountNumber() throws SQLException {
    String sql = "SELECT MAX(CAST(SUBSTRING(account_number, 3) AS UNSIGNED)) AS maxNum FROM accounts";
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            int max = rs.getInt("maxNum");
            return max + 1;
        }
        return 1001;
    }
    }
    public String getPinHash(String accountNumber) throws SQLException {
    String sql = "SELECT pin_hash FROM accounts WHERE account_number = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, accountNumber);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("pin_hash");
            }
            return null;
        }
    }
}

public String getAccountHolder(String accountNumber) throws SQLException {
    String sql = "SELECT account_holder FROM accounts WHERE account_number = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, accountNumber);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("account_holder");
            }
            return null;
        }
    }
}
public boolean accountExists(String accountNumber) throws SQLException {
    String sql = "SELECT 1 FROM accounts WHERE account_number = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, accountNumber);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}
public void transferFunds(String fromAccount, String toAccount, double amount) throws SQLException {
    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false); // Start the transaction

        // Debit sender
        double fromBalance = getBalanceWithConnection(conn, fromAccount);
        double newFromBalance = fromBalance - amount;
        updateBalanceWithConnection(conn, fromAccount, newFromBalance);
        logTransactionWithConnection(conn, fromAccount, "TRANSFER_OUT", amount, newFromBalance, "Transfer to " + toAccount);

        // Credit recipient
        double toBalance = getBalanceWithConnection(conn, toAccount);
        double newToBalance = toBalance + amount;
        updateBalanceWithConnection(conn, toAccount, newToBalance);
        logTransactionWithConnection(conn, toAccount, "TRANSFER_IN", amount, newToBalance, "Transfer from " + fromAccount);

        conn.commit(); // Everything succeeded — make it permanent

    } catch (SQLException e) {
        if (conn != null) {
            conn.rollback(); // Something failed — undo everything
        }
        throw e;
    } finally {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}

// Helper methods that reuse the SAME connection (needed for a transaction to work)
private double getBalanceWithConnection(Connection conn, String accountNumber) throws SQLException {
    String sql = "SELECT balance FROM accounts WHERE account_number = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, accountNumber);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble("balance");
            throw new SQLException("Account not found: " + accountNumber);
        }
    }
}

private void updateBalanceWithConnection(Connection conn, String accountNumber, double newBalance) throws SQLException {
    String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setDouble(1, newBalance);
        ps.setString(2, accountNumber);
        ps.executeUpdate();
    }
}

private void logTransactionWithConnection(Connection conn, String accountNumber, String type, double amount, double balanceAfter, String category) throws SQLException {
    String sql = "INSERT INTO transactions (account_number, type, amount, balance_after, txn_time, category) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, accountNumber);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.setDouble(4, balanceAfter);
        ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(6, category);
        ps.executeUpdate();
    }
}
}
