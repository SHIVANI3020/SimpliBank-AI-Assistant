package bank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

public class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() throws SQLException {
        // Fresh account before every test, using a unique number so tests don't clash
        String testAccountNumber = "TEST" + System.currentTimeMillis();
        account = new Account("Test User", testAccountNumber, 1000.0);
        account.persistNewAccount("0000");
    }

    @Test
    void depositIncreasesBalance() throws SQLException {
        account.deposit(500, "Other");
        assertEquals(1500.0, account.getBalance(), 0.001);
    }

    @Test
    void withdrawDecreasesBalance() throws Exception {
        account.withdraw(300, "Other");
        assertEquals(700.0, account.getBalance(), 0.001);
    }

    @Test
    void withdrawMoreThanBalanceThrowsException() {
        assertThrows(InsufficientFundsException.class, () -> {
            account.withdraw(5000, "Other");
        });
    }

    @Test
    void depositNegativeAmountThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            account.deposit(-100, "Other");
        });
    }

    @Test
    void withdrawNegativeAmountThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            account.withdraw(-50, "Other");
        });
    }

    @Test
    void newAccountHasCorrectInitialBalance() {
        assertEquals(1000.0, account.getBalance(), 0.001);
    }
    @Test
void loginWithCorrectPinSucceeds() throws Exception {
    Account loggedIn = Account.login(account.getAccountNumber(), "0000");
    assertEquals(account.getAccountNumber(), loggedIn.getAccountNumber());
    assertEquals(1000.0, loggedIn.getBalance(), 0.001);
}

@Test
void loginWithWrongPinThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        Account.login(account.getAccountNumber(), "9999");
    });
}

@Test
void loginWithNonexistentAccountThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        Account.login("SB_DOES_NOT_EXIST", "0000");
    });
}

@Test
void transferMovesMoneyBetweenAccounts() throws Exception {
    String recipientNumber = "TR" + System.currentTimeMillis();
    Account recipient = new Account("Recipient User", recipientNumber, 100.0);
    recipient.persistNewAccount("1111");

    account.transferTo(recipientNumber, 200);

    assertEquals(800.0, account.getBalance(), 0.001);

    Account recipientCheck = Account.login(recipientNumber, "1111");
    assertEquals(300.0, recipientCheck.getBalance(), 0.001);
}

@Test
void transferToNonexistentAccountThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        account.transferTo("SB_DOES_NOT_EXIST", 100);
    });
}

@Test
void transferMoreThanBalanceThrowsException() throws Exception {
    String recipientNumber = "TR2" + System.currentTimeMillis();
    Account recipient = new Account("Recipient User", recipientNumber, 100.0);
    recipient.persistNewAccount("2222");

    assertThrows(InsufficientFundsException.class, () -> {
        account.transferTo(recipientNumber, 999999);
    });
}
}