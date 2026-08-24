package com.example.accounts.dao;

import com.example.accounts.domain.LedgerEntry;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class LedgerDao {
    private final Connection connection;

    public LedgerDao(Connection connection) {
        this.connection = connection;
    }

    public int record(int accountId, BigDecimal amount, String description, LocalDateTime occurredAt) throws SQLException {
        String sql = "INSERT INTO ledger_entries (account_id, amount, description, occurred_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, accountId);
            stmt.setBigDecimal(2, amount);
            stmt.setString(3, description);
            stmt.setTimestamp(4, Timestamp.valueOf(occurredAt));
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Most recent first — what a vendor sees as their invoice/payment history. */
    public List<LedgerEntry> findHistory(int accountId) throws SQLException {
        String sql = "SELECT id, account_id, amount, description, occurred_at FROM ledger_entries WHERE account_id = ? ORDER BY occurred_at DESC";
        List<LedgerEntry> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new LedgerEntry(
                            rs.getInt("id"), rs.getInt("account_id"), rs.getBigDecimal("amount"),
                            rs.getString("description"), rs.getTimestamp("occurred_at").toLocalDateTime()
                    ));
                }
            }
        }
        return results;
    }

    /** Computed in the database, not by summing every row in Java — see README "why the balance isn't a column". */
    public BigDecimal computeAmountOwed(int accountId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS amount_owed FROM ledger_entries WHERE account_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("amount_owed");
            }
        }
    }
}
