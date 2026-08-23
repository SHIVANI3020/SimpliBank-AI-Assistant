package bank;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NLQueryEngine {

    private final GeminiService gemini = new GeminiService();

    private static final String SCHEMA_DESCRIPTION = """
        Table accounts(account_number VARCHAR PRIMARY KEY, account_holder VARCHAR, balance DECIMAL)
        Table transactions(id INT, account_number VARCHAR, type VARCHAR('DEPOSIT' or 'WITHDRAW'), amount DECIMAL, balance_after DECIMAL, txn_time DATETIME)
        """;

    public String generateSQL(String question, String accountNumber) throws Exception {
        String prompt = """
            You are a MySQL expert. Given this database schema:
            %s

            Write ONE MySQL SELECT query that answers this question:
            "%s"

            RULES:
            - Only generate a SELECT query. Never INSERT, UPDATE, DELETE, DROP, ALTER, or TRUNCATE.
            - Always filter results to account_number = '%s' only.
            - Return ONLY the raw SQL text. No markdown code fences, no explanation, no semicolon at the end.
            """.formatted(SCHEMA_DESCRIPTION, question, accountNumber);

        String rawResponse = gemini.ask(prompt);
        return cleanSQL(rawResponse);
    }

    private String cleanSQL(String response) {
        return response.trim()
                .replaceAll("```sql", "")
                .replaceAll("```", "")
                .trim();
    }

    public boolean isSafeSelect(String sql) {
        String upper = sql.trim().toUpperCase();
        if (!upper.startsWith("SELECT")) {
            return false;
        }
        String[] forbidden = {"INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "TRUNCATE", "CREATE", "GRANT", "REVOKE", ";"};
        for (String word : forbidden) {
            if (upper.contains(word)) {
                return false;
            }
        }
        return true;
    }

    public List<String> runQuery(String sql) throws SQLException {
        List<String> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    row.append(meta.getColumnName(i)).append("=").append(rs.getString(i));
                    if (i < columnCount) row.append(", ");
                }
                rows.add(row.toString());
            }
        }
        return rows;
    }
    public String interpretResults(String question, List<String> rawRows) throws Exception {
        if (rawRows.isEmpty()) {
            return "I couldn't find any matching transactions for that.";
        }

        String rowsAsText = String.join("\n", rawRows);

        String prompt = """
            A user asked this question about their bank account:
            "%s"

            Here is the raw database result:
            %s

            Write ONE short, friendly sentence (like a bank app notification) that answers their question using this data.
            Use "Rs." for currency. Do not mention SQL, databases, or column names. Just answer naturally.
            """.formatted(question, rowsAsText);

        return gemini.ask(prompt);
    }
    public String generateBudgetingSuggestion(String accountNumber) throws Exception {
    String sql = String.format(
        "SELECT category, SUM(amount) as total FROM transactions WHERE account_number = '%s' AND type = 'WITHDRAW' GROUP BY category",
        accountNumber
    );

    List<String> categoryTotals = runQuery(sql);

    if (categoryTotals.isEmpty()) {
        return "Not enough spending data yet to suggest a budget.";
    }

    String dataAsText = String.join("\n", categoryTotals);

    String prompt = """
        Here is a user's spending broken down by category:
        %s

        Based on this, write 2-3 short, friendly sentences:
        1. Point out their biggest spending category.
        2. Suggest one realistic monthly savings goal (a specific Rs. amount).
        Keep it encouraging, not preachy. Use "Rs." for currency.
        """.formatted(dataAsText);

    return gemini.ask(prompt);
}

}
