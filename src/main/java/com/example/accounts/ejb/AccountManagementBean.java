package com.example.accounts.ejb;

import com.example.accounts.dao.AccountDao;
import com.example.accounts.dao.ProfileDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.CustomerAccount;
import com.example.accounts.domain.CustomerProfile;
import com.example.accounts.domain.Role;
import com.example.accounts.security.PasswordHasher;

import javax.ejb.Stateless;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * The business layer for account management — registration, role-based
 * authentication, deactivation. A real deployment injects its Connection
 * via a container-managed DataSource (@Resource); here each method opens
 * and closes its own via DataSourceConfig, the same pattern the DAO layer
 * and my other J2EE-era projects use — see README "About the EJB layer"
 * for why this bean is unit-tested by direct instantiation rather than
 * through a live container.
 */
@Stateless
public class AccountManagementBean implements AccountManagementLocal {

    @Override
    public int registerCustomer(String username, String plaintextPassword, String fullName, String email) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);

            AccountDao accountDao = new AccountDao(conn);
            if (accountDao.findByUsername(username).isPresent()) {
                throw new IllegalStateException("username already taken: " + username);
            }

            String salt = PasswordHasher.generateSalt();
            String hash = PasswordHasher.hash(plaintextPassword, salt);
            int accountId = accountDao.create(username, hash, salt, Role.CUSTOMER);

            new ProfileDao(conn).create(accountId, fullName, email, null, null);
            return accountId;
        }
    }

    @Override
    public Optional<CustomerAccount> authenticate(String username, String plaintextPassword) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);

            Optional<CustomerAccount> found = new AccountDao(conn).findByUsername(username);
            if (found.isEmpty()) {
                return Optional.empty();
            }
            CustomerAccount account = found.get();
            if (!account.isActive()) {
                return Optional.empty();
            }
            if (!PasswordHasher.matches(plaintextPassword, account.getPasswordSalt(), account.getPasswordHash())) {
                return Optional.empty();
            }
            return Optional.of(account);
        }
    }

    @Override
    public boolean hasRole(CustomerAccount account, Role required) {
        return account.getRole() == required;
    }

    @Override
    public void deactivateAccount(int accountId) throws SQLException {
        setAccountActive(accountId, false);
    }

    @Override
    public Optional<CustomerProfile> getProfile(int accountId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new ProfileDao(conn).findByAccountId(accountId);
        }
    }

    @Override
    public void updateProfile(int accountId, String phone, String address) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            new ProfileDao(conn).updateContactDetails(accountId, phone, address);
        }
    }

    @Override
    public List<CustomerAccount> findAllAccounts() throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new AccountDao(conn).findAll();
        }
    }

    @Override
    public void setAccountActive(int accountId, boolean active) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            new AccountDao(conn).setActive(accountId, active);
        }
    }
}
