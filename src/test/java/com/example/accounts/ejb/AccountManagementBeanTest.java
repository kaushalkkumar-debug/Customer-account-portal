package com.example.accounts.ejb;

import com.example.accounts.dao.AccountDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.CustomerAccount;
import com.example.accounts.domain.Role;
import com.example.accounts.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
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
class AccountManagementBeanTest {
    // A unique username per test avoids collisions in the single shared
    // in-memory DB this JVM run's DataSourceConfig points at (see that
    // class's Javadoc — it's realistic for a real deployment to share
    // one DB across every bean call, so tests just need unique data).
    private static String uniqueUsername() {
        return "user-" + UUID.randomUUID();
    }

    @Test
    void registerCustomerThenAuthenticateSucceedsWithTheRightPassword() throws SQLException {
        AccountManagementLocal bean = new AccountManagementBean();
        String username = uniqueUsername();

        bean.registerCustomer(username, "correct-password", "Jamie Test", "jamie@example.com");

        Optional<CustomerAccount> account = bean.authenticate(username, "correct-password");

        assertTrue(account.isPresent());
        assertEquals(username, account.get().getUsername());
        assertEquals(Role.CUSTOMER, account.get().getRole());
    }

    @Test
    void authenticateFailsWithTheWrongPassword() throws SQLException {
        AccountManagementLocal bean = new AccountManagementBean();
        String username = uniqueUsername();
        bean.registerCustomer(username, "correct-password", "Jamie Test", "jamie@example.com");

        assertTrue(bean.authenticate(username, "wrong-password").isEmpty());
    }

    @Test
    void authenticateFailsForAnUnknownUsername() throws SQLException {
        AccountManagementLocal bean = new AccountManagementBean();
        assertTrue(bean.authenticate("no-such-user-" + UUID.randomUUID(), "anything").isEmpty());
    }

    @Test
    void registeringTheSameUsernameTwiceThrows() throws SQLException {
        AccountManagementLocal bean = new AccountManagementBean();
        String username = uniqueUsername();
        bean.registerCustomer(username, "password1", "First", "first@example.com");

        assertThrows(IllegalStateException.class,
                () -> bean.registerCustomer(username, "password2", "Second", "second@example.com"));
    }

    @Test
    void deactivatedAccountsCannotAuthenticateEvenWithTheCorrectPassword() throws SQLException {
        AccountManagementLocal bean = new AccountManagementBean();
        String username = uniqueUsername();
        int accountId = bean.registerCustomer(username, "correct-password", "Jamie Test", "jamie@example.com");

        bean.deactivateAccount(accountId);

        assertTrue(bean.authenticate(username, "correct-password").isEmpty());
    }

    @Test
    void hasRoleCorrectlyDistinguishesCustomerFromAdmin() throws SQLException {
        AccountManagementLocal bean = new AccountManagementBean();
        String username = uniqueUsername();
        bean.registerCustomer(username, "password", "Jamie Test", "jamie@example.com");
        CustomerAccount customer = bean.authenticate(username, "password").orElseThrow();

        assertTrue(bean.hasRole(customer, Role.CUSTOMER));
        assertFalse(bean.hasRole(customer, Role.ADMIN));
    }

    @Test
    void hasRoleRecognizesAnAdminAccount() throws SQLException {
        // Admin accounts aren't created via registerCustomer (which always
        // creates a CUSTOMER) — seed one directly via the DAO, the way a
        // real deployment's admin-provisioning path would.
        String username = uniqueUsername();
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            String salt = PasswordHasher.generateSalt();
            new AccountDao(conn).create(username, PasswordHasher.hash("adminpass", salt), salt, Role.ADMIN);
        }

        AccountManagementLocal bean = new AccountManagementBean();
        CustomerAccount admin = bean.authenticate(username, "adminpass").orElseThrow();

        assertTrue(bean.hasRole(admin, Role.ADMIN));
        assertFalse(bean.hasRole(admin, Role.CUSTOMER));
    }
}
