package com.example.accounts.dao;

import com.example.accounts.domain.CustomerProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class ProfileDao {
    private final Connection connection;

    public ProfileDao(Connection connection) {
        this.connection = connection;
    }

    public void create(int accountId, String fullName, String email, String phone, String address) throws SQLException {
        String sql = "INSERT INTO profiles (account_id, full_name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setString(2, fullName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, address);
            stmt.executeUpdate();
        }
    }

    public Optional<CustomerProfile> findByAccountId(int accountId) throws SQLException {
        String sql = "SELECT account_id, full_name, email, phone, address FROM profiles WHERE account_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new CustomerProfile(
                        rs.getInt("account_id"), rs.getString("full_name"),
                        rs.getString("email"), rs.getString("phone"), rs.getString("address")
                ));
            }
        }
    }

    public void updateContactDetails(int accountId, String phone, String address) throws SQLException {
        String sql = "UPDATE profiles SET phone = ?, address = ? WHERE account_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phone);
            stmt.setString(2, address);
            stmt.setInt(3, accountId);
            stmt.executeUpdate();
        }
    }
}
