package com.example.accounts.ejb;

import com.example.accounts.dao.VendorDao;
import com.example.accounts.dao.VendorProfileDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorAccount;
import com.example.accounts.domain.VendorCategory;
import com.example.accounts.domain.VendorProfile;
import com.example.accounts.security.PasswordHasher;

import javax.ejb.Stateless;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * The business layer for vendor management — registration, role-based
 * authentication, the approval workflow, deactivation. A real deployment
 * injects its Connection via a container-managed DataSource (@Resource);
 * here each method opens and closes its own via DataSourceConfig, the
 * same pattern the DAO layer and my other J2EE-era projects use — see
 * README "About the EJB layer" for why this bean is unit-tested by
 * direct instantiation rather than through a live container.
 */
@Stateless
public class VendorManagementBean implements VendorManagementLocal {

    @Override
    public int registerVendor(String username, String plaintextPassword, String companyName, VendorCategory category,
                               String contactName, String email) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);

            VendorDao vendorDao = new VendorDao(conn);
            if (vendorDao.findByUsername(username).isPresent()) {
                throw new IllegalStateException("username already taken: " + username);
            }

            String salt = PasswordHasher.generateSalt();
            String hash = PasswordHasher.hash(plaintextPassword, salt);
            // Every self-registered vendor starts PENDING, regardless of
            // what the form sends — a vendor can't approve themselves.
            int accountId = vendorDao.create(username, hash, salt, Role.VENDOR, companyName, category, ApprovalStatus.PENDING);

            new VendorProfileDao(conn).create(accountId, contactName, email, null, null);
            return accountId;
        }
    }

    @Override
    public Optional<VendorAccount> authenticate(String username, String plaintextPassword) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);

            Optional<VendorAccount> found = new VendorDao(conn).findByUsername(username);
            if (found.isEmpty()) {
                return Optional.empty();
            }
            VendorAccount account = found.get();
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
    public boolean hasRole(VendorAccount account, Role required) {
        return account.getRole() == required;
    }

    @Override
    public void deactivateAccount(int accountId) throws SQLException {
        setAccountActive(accountId, false);
    }

    @Override
    public Optional<VendorAccount> getAccount(int accountId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new VendorDao(conn).findById(accountId);
        }
    }

    @Override
    public Optional<VendorProfile> getProfile(int accountId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new VendorProfileDao(conn).findByAccountId(accountId);
        }
    }

    @Override
    public void updateProfile(int accountId, String phone, String address) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            new VendorProfileDao(conn).updateContactDetails(accountId, phone, address);
        }
    }

    @Override
    public List<VendorAccount> findAllAccounts() throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new VendorDao(conn).findAll();
        }
    }

    @Override
    public void setAccountActive(int accountId, boolean active) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            new VendorDao(conn).setActive(accountId, active);
        }
    }

    @Override
    public void setApprovalStatus(int accountId, ApprovalStatus status) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            new VendorDao(conn).setApprovalStatus(accountId, status);
        }
    }
}
