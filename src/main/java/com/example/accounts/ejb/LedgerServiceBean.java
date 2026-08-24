package com.example.accounts.ejb;

import com.example.accounts.dao.LedgerDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.LedgerEntry;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/** The accounts-payable ledger business layer — records invoices/payments and reports the running amount owed. */
@Stateless
public class LedgerServiceBean implements LedgerServiceLocal {

    @Override
    public int recordEntry(int accountId, BigDecimal amount, String description) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new LedgerDao(conn).record(accountId, amount, description, LocalDateTime.now());
        }
    }

    @Override
    public List<LedgerEntry> getLedgerHistory(int accountId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new LedgerDao(conn).findHistory(accountId);
        }
    }

    @Override
    public BigDecimal getAmountOwed(int accountId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new LedgerDao(conn).computeAmountOwed(accountId);
        }
    }
}
