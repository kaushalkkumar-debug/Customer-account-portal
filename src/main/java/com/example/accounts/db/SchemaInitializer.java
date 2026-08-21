package com.example.accounts.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {
    public static void ensureSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(100) NOT NULL,
                    password_salt VARCHAR(50) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS profiles (
                    account_id INT PRIMARY KEY REFERENCES accounts(id),
                    full_name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    phone VARCHAR(30),
                    address VARCHAR(300)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    account_id INT NOT NULL REFERENCES accounts(id),
                    amount DECIMAL(12,2) NOT NULL,
                    description VARCHAR(300) NOT NULL,
                    occurred_at TIMESTAMP NOT NULL
                )
                """);
            // The transaction-history query is always "this account, most
            // recent first" — index the FK it's filtered by.
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id)");
        }
    }

    private SchemaInitializer() {
    }
}
