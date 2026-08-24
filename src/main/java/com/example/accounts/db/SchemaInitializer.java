package com.example.accounts.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {
    public static void ensureSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vendors (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(100) NOT NULL,
                    password_salt VARCHAR(50) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    company_name VARCHAR(150) NOT NULL,
                    category VARCHAR(30),
                    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vendor_profiles (
                    account_id INT PRIMARY KEY REFERENCES vendors(id),
                    contact_name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    phone VARCHAR(30),
                    address VARCHAR(300)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ledger_entries (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    account_id INT NOT NULL REFERENCES vendors(id),
                    amount DECIMAL(12,2) NOT NULL,
                    description VARCHAR(300) NOT NULL,
                    occurred_at TIMESTAMP NOT NULL
                )
                """);
            // The ledger-history query is always "this vendor, most recent
            // first" — index the FK it's filtered by.
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_ledger_entries_account_id ON ledger_entries(account_id)");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vendor_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    account_id INT NOT NULL REFERENCES vendors(id),
                    name VARCHAR(150) NOT NULL,
                    category VARCHAR(30) NOT NULL,
                    unit_price DECIMAL(12,2) NOT NULL,
                    description VARCHAR(500),
                    active BOOLEAN NOT NULL DEFAULT TRUE
                )
                """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_vendor_items_account_id ON vendor_items(account_id)");
        }
    }

    private SchemaInitializer() {
    }
}
