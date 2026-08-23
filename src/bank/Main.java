package bank;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Welcome to SimpliBank ===");
System.out.println("1. Create new account   2. Login to existing account");
System.out.print("Choose an option: ");
int startChoice = readInt();

Account account;

if (startChoice == 1) {
    System.out.print("Enter your name to open an account: ");
    String name = scanner.nextLine();
    System.out.print("Enter initial deposit amount: Rs.");
    double initial = readDouble();
    System.out.print("Set a 4-digit PIN: ");
    String pin = scanner.nextLine();

    try {
        AccountDAO tempDao = new AccountDAO();
        int nextNum = tempDao.getNextAccountNumber();
        String accountNumber = "SB" + nextNum;
        account = new Account(name, accountNumber, initial);
        account.persistNewAccount(pin);
    } catch (SQLException e) {
        System.out.println("Could not save account to database: " + e.getMessage());
        return;
    }
    System.out.println("Account created! Account number: " + account.getAccountNumber());

} else {
    System.out.print("Enter your account number: ");
    String accountNumber = scanner.nextLine();
    System.out.print("Enter your PIN: ");
    String pin = scanner.nextLine();

    try {
        account = Account.login(accountNumber, pin);
        System.out.println("Login successful! Welcome back, " + account.getAccountHolder() + ".");
    } catch (IllegalArgumentException | SQLException e) {
        System.out.println("Login failed: " + e.getMessage());
        return;
    }
}

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt();

            switch (choice) {
                case 1 -> System.out.printf("Current Balance: Rs.%.2f%n", account.getBalance());

                case 2 -> {
    System.out.print("Enter deposit amount: Rs.");
    double amount = readDouble();
    System.out.print("Enter category (e.g. Salary, Gift, Other): ");
    String category = scanner.nextLine();
    try {
        account.deposit(amount, category);
        System.out.printf("Deposit successful! New Balance: Rs.%.2f%n", account.getBalance());
    } catch (IllegalArgumentException | SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}

                case 3 -> {
    System.out.print("Enter withdrawal amount: Rs.");
    double amount = readDouble();
    System.out.print("Enter category (e.g. Food, Bills, Shopping, Other): ");
    String category = scanner.nextLine();
    try {
        String warning = account.checkForUnusualSpending(amount);
        account.withdraw(amount, category);
        System.out.printf("Withdrawal successful! New Balance: Rs.%.2f%n", account.getBalance());
        if (warning != null) {
            System.out.println("⚠️ " + warning);
        }
    } catch (InsufficientFundsException | IllegalArgumentException | SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}

                case 4 -> {
                    System.out.println("--- Transaction History ---");
                    try {
                        List<Transaction> history = account.getTransactionHistory();
                        if (history.isEmpty()) {
                            System.out.println("No transactions yet.");
                        } else {
                            history.forEach(System.out::println);
                        }
                    } catch (SQLException e) {
                        System.out.println("Could not load history: " + e.getMessage());
                    }
                }

                case 5 -> {
                     System.out.print("Ask a question about your account: ");
                     String question = scanner.nextLine();
                     try {
                         NLQueryEngine engine = new NLQueryEngine();
                         String sql = engine.generateSQL(question, account.getAccountNumber());
                         System.out.println("Generated SQL: " + sql);

                         if (!engine.isSafeSelect(sql)) {
                             System.out.println("⚠️ That query looked unsafe, so I didn't run it.");
                         } else {
                             List<String> results = engine.runQuery(sql);
                             String friendlyAnswer = engine.interpretResults(question, results);
                             System.out.println("💬 " + friendlyAnswer);
                         }
                     } catch (Exception e) {
                         System.out.println("AI query failed: " + e.getMessage());
                     }
                }

                case 6 -> {
                    try {
                        NLQueryEngine engine = new NLQueryEngine();
                        String suggestion = engine.generateBudgetingSuggestion(account.getAccountNumber());
                        System.out.println("💡 " + suggestion);
                    } catch (Exception e) {
                        System.out.println("Could not generate suggestion: " + e.getMessage());
                    }
                }

                case 7 -> {
    System.out.print("Enter recipient's account number: ");
    String recipient = scanner.nextLine();
    System.out.print("Enter amount to transfer: Rs.");
    double amount = readDouble();
    try {
        account.transferTo(recipient, amount);
        System.out.printf("Transfer successful! New Balance: Rs.%.2f%n", account.getBalance());
    } catch (InsufficientFundsException | IllegalArgumentException | SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}

case 8 -> {
    running = false;
    System.out.println("Thank you for using SimpliBank. Goodbye!");
}
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }
    private static void printMenu() {
    System.out.println("\n1. Check Balance  2. Deposit  3. Withdraw  4. Transaction History  5. Ask AI  6. Budget Suggestions  7. Transfer  8. Exit");
    System.out.print("Choose an option: ");
}
    private static int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }

    private static double readDouble() {
        while (!scanner.hasNextDouble()) {
            System.out.print("Please enter a valid amount: ");
            scanner.next();
        }
        double val = scanner.nextDouble();
        scanner.nextLine();
        return val;
    }
}

