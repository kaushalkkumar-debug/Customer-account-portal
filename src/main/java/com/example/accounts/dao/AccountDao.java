package com.example.accounts.dao;

import com.example.accounts.domain.CustomerAccount;
import com.example.accounts.domain.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AccountDao {
    private final Connection connection;

    public AccountDao(Connection connection) {
        this.connection = connection;
    }

    public int create(String username, String passwordHash, String passwordSalt, Role role) throws SQLException {
        String sql = "INSERT INTO accounts (username, password_hash, password_salt, role, active) VALUES (?, ?, ?, ?, TRUE)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, passwordSalt);
            stmt.setString(4, role.name());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Optional<CustomerAccount> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, password_salt, role, active FROM accounts WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<CustomerAccount> findById(int id) throws SQLException {
        String sql = "SELECT id, username, password_hash, password_salt, role, active FROM accounts WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** For the admin account-management screen — every account, oldest first. */
    public List<CustomerAccount> findAll() throws SQLException {
        String sql = "SELECT id, username, password_hash, password_salt, role, active FROM accounts ORDER BY id";
        List<CustomerAccount> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    public void setActive(int accountId, boolean active) throws SQLException {
        String sql = "UPDATE accounts SET active = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    private CustomerAccount mapRow(ResultSet rs) throws SQLException {
        return new CustomerAccount(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("password_salt"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("active")
        );
    }
}
