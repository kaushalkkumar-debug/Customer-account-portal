package com.example.accounts.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Same config-gated JDBC connection pattern as my other J2EE-era
 * projects — see README "About the database layer". The default H2
 * database name is randomized once per JVM run (not per connection —
 * every AccountManagementBean/TransactionServiceBean call within one
 * test run correctly shares the same in-memory DB, exactly like a real
 * deployment sharing one real database) so separate test runs never see
 * leftover data from a previous run.
 */
public final class DataSourceConfig {
    private static final String DEFAULT_DB_NAME = "accountportal-" + UUID.randomUUID();
    public static final String DEFAULT_URL = "jdbc:h2:mem:" + DEFAULT_DB_NAME + ";DB_CLOSE_DELAY=-1";
    public static final String DEFAULT_DRIVER = "org.h2.Driver";

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("ACCOUNTS_DB_URL", DEFAULT_URL);
        String driver = System.getenv().getOrDefault("ACCOUNTS_DB_DRIVER", DEFAULT_DRIVER);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not on classpath: " + driver, e);
        }
        String user = System.getenv().getOrDefault("ACCOUNTS_DB_USER", "sa");
        String password = System.getenv().getOrDefault("ACCOUNTS_DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    private DataSourceConfig() {
    }
}
