package com.example.accounts.dao;

import com.example.accounts.domain.ItemCategory;
import com.example.accounts.domain.VendorItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VendorItemDao {
    private final Connection connection;

    public VendorItemDao(Connection connection) {
        this.connection = connection;
    }

    public int create(int accountId, String name, ItemCategory category, BigDecimal unitPrice, String description) throws SQLException {
        String sql = "INSERT INTO vendor_items (account_id, name, category, unit_price, description, active) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, accountId);
            stmt.setString(2, name);
            stmt.setString(3, category.name());
            stmt.setBigDecimal(4, unitPrice);
            stmt.setString(5, description);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Optional<VendorItem> findById(int itemId) throws SQLException {
        String sql = "SELECT id, account_id, name, category, unit_price, description, active FROM vendor_items WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** A vendor's own catalog, for their dashboard's "My catalog" section — active and inactive alike. */
    public List<VendorItem> findByVendor(int accountId) throws SQLException {
        String sql = "SELECT id, account_id, name, category, unit_price, description, active FROM vendor_items WHERE account_id = ? ORDER BY id";
        List<VendorItem> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * Everything procurement can browse and buy: active items belonging to
     * APPROVED, active vendors only. A rejected or deactivated vendor's
     * listings simply don't appear here — the join enforces the approval
     * gate at the catalog level, not just at login.
     */
    public List<VendorItem> findPurchasableCatalog() throws SQLException {
        String sql = """
            SELECT i.id, i.account_id, i.name, i.category, i.unit_price, i.description, i.active
            FROM vendor_items i
            JOIN vendors v ON v.id = i.account_id
            WHERE i.active = TRUE AND v.active = TRUE AND v.approval_status = 'APPROVED'
            ORDER BY i.category, i.name
            """;
        List<VendorItem> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    public void setActive(int itemId, boolean active) throws SQLException {
        String sql = "UPDATE vendor_items SET active = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        }
    }

    private VendorItem mapRow(ResultSet rs) throws SQLException {
        return new VendorItem(
                rs.getInt("id"),
                rs.getInt("account_id"),
                rs.getString("name"),
                ItemCategory.valueOf(rs.getString("category")),
                rs.getBigDecimal("unit_price"),
                rs.getString("description"),
                rs.getBoolean("active")
        );
    }
}
