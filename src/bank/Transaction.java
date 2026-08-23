package bank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final String category;
    private final LocalDateTime timestamp;

    public Transaction(String type, double amount, double balanceAfter, String category) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.category = category;
        this.timestamp = LocalDateTime.now();
    }

    public String getCategory() {
        return category;
    }
    public double getAmount() {
         return amount;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return String.format("[%s] %-10s Rs.%-10.2f Balance: Rs.%-10.2f Category: %s",
                timestamp.format(fmt), type, amount, balanceAfter, category);
    }
}