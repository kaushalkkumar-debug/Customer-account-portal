package com.example.accounts.ejb;

import com.example.accounts.dao.TransactionDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.Transaction;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/** The "transaction history" business layer — records activity and reports on it. */
@Stateless
public class TransactionServiceBean implements TransactionServiceLocal {

    @Override
    public int recordTransaction(int accountId, BigDecimal amount, String description) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new TransactionDao(conn).record(accountId, amount, description, LocalDateTime.now());
        }
    }

    @Override
    public List<Transaction> getTransactionHistory(int accountId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new TransactionDao(conn).findHistory(accountId);
        }
    }

    @Override
    public BigDecimal getCurrentBalance(int accountId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new TransactionDao(conn).computeBalance(accountId);
        }
    }
}
