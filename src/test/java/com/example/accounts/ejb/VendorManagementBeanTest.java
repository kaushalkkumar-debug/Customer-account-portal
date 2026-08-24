package com.example.accounts.ejb;

import com.example.accounts.dao.VendorDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorAccount;
import com.example.accounts.domain.VendorCategory;
import com.example.accounts.domain.VendorProfile;
import com.example.accounts.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the EJB session bean directly — a standard way to unit-test a
 * stateless session bean's business logic without a live container. See
 * README "About the EJB layer" for why.
 */
class VendorManagementBeanTest {
    // A unique username per test avoids collisions in the single shared
    // in-memory DB this JVM run's DataSourceConfig points at (see that
    // class's Javadoc — it's realistic for a real deployment to share
    // one DB across every bean call, so tests just need unique data).
    private static String uniqueUsername() {
        return "vendor-" + UUID.randomUUID();
    }

    @Test
    void registerVendorThenAuthenticateSucceedsWithTheRightPassword() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();

        bean.registerVendor(username, "correct-password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        Optional<VendorAccount> account = bean.authenticate(username, "correct-password");

        assertTrue(account.isPresent());
        assertEquals(username, account.get().getUsername());
        assertEquals(Role.VENDOR, account.get().getRole());
    }

    @Test
    void registerVendorAlwaysStartsPending() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();

        int accountId = bean.registerVendor(username, "password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        VendorAccount account = bean.getAccount(accountId).orElseThrow();
        assertEquals(ApprovalStatus.PENDING, account.getApprovalStatus());
        assertFalse(account.isApproved());
    }

    @Test
    void setApprovalStatusMovesAVendorFromPendingToApproved() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        int accountId = bean.registerVendor(username, "password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        bean.setApprovalStatus(accountId, ApprovalStatus.APPROVED);

        VendorAccount account = bean.getAccount(accountId).orElseThrow();
        assertEquals(ApprovalStatus.APPROVED, account.getApprovalStatus());
        assertTrue(account.isApproved());
    }

    @Test
    void aPendingVendorCanStillAuthenticate() throws SQLException {
        // Logging in isn't gated on approval — a pending vendor needs to
        // be able to see their own application status.
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        bean.registerVendor(username, "password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        assertTrue(bean.authenticate(username, "password").isPresent());
    }

    @Test
    void authenticateFailsWithTheWrongPassword() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        bean.registerVendor(username, "correct-password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        assertTrue(bean.authenticate(username, "wrong-password").isEmpty());
    }

    @Test
    void authenticateFailsForAnUnknownUsername() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        assertTrue(bean.authenticate("no-such-vendor-" + UUID.randomUUID(), "anything").isEmpty());
    }

    @Test
    void registeringTheSameUsernameTwiceThrows() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        bean.registerVendor(username, "password1", "First Co", VendorCategory.SUPPLIES, "First", "first@example.com");

        assertThrows(IllegalStateException.class,
                () -> bean.registerVendor(username, "password2", "Second Co", VendorCategory.SERVICES, "Second", "second@example.com"));
    }

    @Test
    void deactivatedAccountsCannotAuthenticateEvenWithTheCorrectPassword() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        int accountId = bean.registerVendor(username, "correct-password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        bean.deactivateAccount(accountId);

        assertTrue(bean.authenticate(username, "correct-password").isEmpty());
    }

    @Test
    void hasRoleCorrectlyDistinguishesVendorFromAdmin() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        bean.registerVendor(username, "password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");
        VendorAccount vendor = bean.authenticate(username, "password").orElseThrow();

        assertTrue(bean.hasRole(vendor, Role.VENDOR));
        assertFalse(bean.hasRole(vendor, Role.ADMIN));
    }

    @Test
    void hasRoleRecognizesAnAdminAccount() throws SQLException {
        // Admin accounts aren't created via registerVendor (which always
        // creates a VENDOR, PENDING) — seed one directly via the DAO, the
        // way a real deployment's admin-provisioning path would.
        String username = uniqueUsername();
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            String salt = PasswordHasher.generateSalt();
            new VendorDao(conn).create(username, PasswordHasher.hash("adminpass", salt), salt, Role.ADMIN, "Procurement Office", null, ApprovalStatus.APPROVED);
        }

        VendorManagementLocal bean = new VendorManagementBean();
        VendorAccount admin = bean.authenticate(username, "adminpass").orElseThrow();

        assertTrue(bean.hasRole(admin, Role.ADMIN));
        assertFalse(bean.hasRole(admin, Role.VENDOR));
    }

    @Test
    void getProfileReturnsWhatWasRegistered() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        int accountId = bean.registerVendor(username, "password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        VendorProfile profile = bean.getProfile(accountId).orElseThrow();

        assertEquals("Jamie Test", profile.getContactName());
        assertEquals("jamie@example.com", profile.getEmail());
    }

    @Test
    void updateProfileChangesPhoneAndAddress() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        int accountId = bean.registerVendor(username, "password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        bean.updateProfile(accountId, "07700 900123", "1 Test Street");

        VendorProfile profile = bean.getProfile(accountId).orElseThrow();
        assertEquals("07700 900123", profile.getPhone());
        assertEquals("1 Test Street", profile.getAddress());
    }

    @Test
    void findAllAccountsIncludesEveryRegisteredVendor() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        int accountId = bean.registerVendor(username, "password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");

        List<VendorAccount> accounts = bean.findAllAccounts();

        assertTrue(accounts.stream().anyMatch(a -> a.getId() == accountId && a.getUsername().equals(username)));
    }

    @Test
    void setAccountActiveCanReactivateADeactivatedAccount() throws SQLException {
        VendorManagementLocal bean = new VendorManagementBean();
        String username = uniqueUsername();
        int accountId = bean.registerVendor(username, "password", "Acme Supplies", VendorCategory.SUPPLIES, "Jamie Test", "jamie@example.com");
        bean.deactivateAccount(accountId);
        assertTrue(bean.authenticate(username, "password").isEmpty());

        bean.setAccountActive(accountId, true);

        assertTrue(bean.authenticate(username, "password").isPresent());
    }
}
