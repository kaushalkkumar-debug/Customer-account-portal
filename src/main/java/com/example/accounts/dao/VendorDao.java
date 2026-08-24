package com.example.accounts.dao;

import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorAccount;
import com.example.accounts.domain.VendorCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VendorDao {
    private final Connection connection;

    public VendorDao(Connection connection) {
        this.connection = connection;
    }

    public int create(String username, String passwordHash, String passwordSalt, Role role,
                       String companyName, VendorCategory category, ApprovalStatus approvalStatus) throws SQLException {
        String sql = "INSERT INTO vendors (username, password_hash, password_salt, role, active, company_name, category, approval_status) " +
                "VALUES (?, ?, ?, ?, TRUE, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, passwordSalt);
            stmt.setString(4, role.name());
            stmt.setString(5, companyName);
            stmt.setString(6, category == null ? null : category.name());
            stmt.setString(7, approvalStatus.name());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Optional<VendorAccount> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, password_salt, role, active, company_name, category, approval_status " +
                "FROM vendors WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<VendorAccount> findById(int id) throws SQLException {
        String sql = "SELECT id, username, password_hash, password_salt, role, active, company_name, category, approval_status " +
                "FROM vendors WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** For the admin console — every vendor, oldest first (pending applications and long-standing vendors alike; the caller splits by approvalStatus). */
    public List<VendorAccount> findAll() throws SQLException {
        String sql = "SELECT id, username, password_hash, password_salt, role, active, company_name, category, approval_status " +
                "FROM vendors ORDER BY id";
        List<VendorAccount> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    public void setActive(int accountId, boolean active) throws SQLException {
        String sql = "UPDATE vendors SET active = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    /** Procurement's approve/reject decision on a pending vendor application. */
    public void setApprovalStatus(int accountId, ApprovalStatus status) throws SQLException {
        String sql = "UPDATE vendors SET approval_status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    private VendorAccount mapRow(ResultSet rs) throws SQLException {
        String categoryValue = rs.getString("category");
        return new VendorAccount(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("password_salt"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("active"),
                rs.getString("company_name"),
                categoryValue == null ? null : VendorCategory.valueOf(categoryValue),
                ApprovalStatus.valueOf(rs.getString("approval_status"))
        );
    }
}
