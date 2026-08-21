package com.example.accounts.ejb;

import com.example.accounts.dao.AccountDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.Role;
import com.example.accounts.domain.Transaction;
import com.example.accounts.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionServiceBeanTest {

    private static int seedAccount() throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            String salt = PasswordHasher.generateSalt();
            return new AccountDao(conn).create("txn-user-" + UUID.randomUUID(), PasswordHasher.hash("pw", salt), salt, Role.CUSTOMER);
        }
    }

    @Test
    void recordedTransactionsAppearInHistoryMostRecentFirst() throws SQLException {
        int accountId = seedAccount();
        TransactionServiceLocal service = new TransactionServiceBean();

        service.recordTransaction(accountId, new BigDecimal("100.00"), "Initial deposit");
        service.recordTransaction(accountId, new BigDecimal("-25.50"), "Purchase");

        List<Transaction> history = service.getTransactionHistory(accountId);

        assertEquals(2, history.size());
        assertEquals("Purchase", history.get(0).getDescription(), "most recent transaction should be first");
    }

    @Test
    void balanceIsTheSumOfAllTransactions() throws SQLException {
        int accountId = seedAccount();
        TransactionServiceLocal service = new TransactionServiceBean();

        service.recordTransaction(accountId, new BigDecimal("100.00"), "Deposit");
        service.recordTransaction(accountId, new BigDecimal("-25.50"), "Purchase");
        service.recordTransaction(accountId, new BigDecimal("10.00"), "Refund");

        BigDecimal balance = service.getCurrentBalance(accountId);

        assertEquals(0, balance.compareTo(new BigDecimal("84.50")), "expected 84.50, was " + balance);
    }

    @Test
    void balanceIsZeroForAnAccountWithNoTransactions() throws SQLException {
        int accountId = seedAccount();
        TransactionServiceLocal service = new TransactionServiceBean();

        assertEquals(0, service.getCurrentBalance(accountId).compareTo(BigDecimal.ZERO));
    }

    @Test
    void historyIsScopedToItsOwnAccount() throws SQLException {
        int accountA = seedAccount();
        int accountB = seedAccount();
        TransactionServiceLocal service = new TransactionServiceBean();

        service.recordTransaction(accountA, new BigDecimal("50.00"), "A's deposit");
        service.recordTransaction(accountB, new BigDecimal("999.00"), "B's deposit");

        List<Transaction> historyA = service.getTransactionHistory(accountA);

        assertEquals(1, historyA.size());
        assertTrue(historyA.stream().allMatch(t -> t.getAccountId() == accountA));
    }
}
